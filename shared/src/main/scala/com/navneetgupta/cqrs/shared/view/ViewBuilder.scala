package com.navneetgupta.cqrs.shared.view

import akka.actor.Stash
import akka.persistence.query.{ PersistenceQuery, EventEnvelope, Offset, Sequence, NoOffset, TimeBasedUUID }
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import scala.concurrent.Future
import java.util.Date
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.stream.{ ActorMaterializerSettings, Supervision, ActorMaterializer }
import scala.util.control.NonFatal
import spray.json.JsonFormat
import scala.reflect.ClassTag
import com.navneetgupta.cqrs.shared.readstore.ElasticsearchApi
import com.navneetgupta.cqrs.shared.actor.BaseActor
import com.navneetgupta.cqrs.shared.readstore.ElasticsearchSupport
import com.navneetgupta.cqrs.shared.eventsource.ResumableProjection

trait ReadModelObject extends AnyRef {
  def id: String
}

object ViewBuilder {
  import ElasticsearchApi._
  sealed trait IndexAction
  case class UpdateAction(id: String, expression: List[String],
                          params: Map[String, Any]) extends IndexAction
  object UpdateAction {
    def apply(id: String, expression: String,
              params: Map[String, Any]): UpdateAction =
      UpdateAction(id, List(expression), params)
  }
  case class InsertAction(id: String,
                          rm: ReadModelObject) extends IndexAction
  case class NoAction(id: String) extends IndexAction
  //case object DeferredCreate extends IndexAction
  case class LatestOffsetResult(offset: Option[Offset])
  case class EnvelopeAndAction(env: EventEnvelope,
                               action: IndexAction)
  case class EnvelopeAndFunction(env: EventEnvelope,
                                 f: () => Future[IndexingResult])
  case class DeferredCreate(
    flow: Flow[EnvelopeAndAction, EnvelopeAndAction, akka.NotUsed])
      extends IndexAction
}

abstract class ViewBuilder[RM <: ReadModelObject: ClassTag] extends BaseActor with ElasticsearchSupport {

  import context.dispatcher
  import ViewBuilder._
  import ElasticsearchApi._
  import akka.pattern.pipe

  val journal = PersistenceQuery(context.system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  val decider: Supervision.Decider = {
    case NonFatal(ex) =>
      log.error(ex, "**************************Got non fatal exception in ViewBuilder flow**************************")
      Supervision.Resume
    case ex =>
      log.error(ex, "**************************Got fatal exception in ViewBuilder flow, stream will be stopped**************************")
      Supervision.Stop
  }

  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(context.system).
      withSupervisionStrategy(decider))

  implicit val rmFormats: JsonFormat[RM]

  val resumableProjection = ResumableProjection(projectionId, context.system)
  resumableProjection.
    fetchLatestOffset.
    map(LatestOffsetResult.apply).
    pipeTo(self)

  def projectionId: String

  def receive = {
    case LatestOffsetResult(offset) =>
      val offsetDate = offset.getOrElse(NoOffset) match {
        case NoOffset =>
          clearIndex
          new Date(0L)
        case TimeBasedUUID(x) =>
          new Date()
      }
      val eventsSource = journal.eventsByTag(entityType, offset.getOrElse(NoOffset))

      eventsSource.via(eventsFlow).runWith(Sink.ignore)
      log.info("**************************Starting up view builder for entity {} with offset time of {}**************************", entityType, offsetDate)
  }

  def actionFor(id: String, eventEnv: EventEnvelope): IndexAction

  val eventsFlow = {
    Flow[EventEnvelope].
      map { env =>
        val id = env.persistenceId.
          toLowerCase().drop(entityType.length() + 1)
        EnvelopeAndAction(env, actionFor(id, env))
      }.
      flatMapConcat {
        case ea @ EnvelopeAndAction(env, cr: DeferredCreate) =>
          Source.single(ea).via(cr.flow)
        case ea: EnvelopeAndAction =>
          Source.single(ea).via(Flow[EnvelopeAndAction])
      }.
      collect {
        case EnvelopeAndAction(env, InsertAction(id, rm: RM)) =>
          EnvelopeAndFunction(env, () => updateIndex(id, rm, None))
        case EnvelopeAndAction(env, u: UpdateAction) =>
          EnvelopeAndFunction(env, () => updateDocumentField(u.id, env.sequenceNr - 1, u.expression, u.params))
        case EnvelopeAndAction(env, NoAction(id)) =>
          EnvelopeAndFunction(env, () => updateDocumentField(id, env.sequenceNr - 1, Nil, Map.empty[String, Any]))
      }.
      //      groupBy() {
      //
      //      }.
      mapAsync(1) {
        case EnvelopeAndFunction(env, f) => f.apply.map(_ => env)
      }.
      mapAsync(1) { env =>
        env.offset match {
          case TimeBasedUUID(x) =>
            resumableProjection.storeLatestOffset(x)
        }
      }
  }

  def updateDocumentField(id: String, seq: Long, expressions: List[String], params: Map[String, Any]): Future[IndexingResult] = {

    val script = expressions.map(e => {
      log.info("expressions is {} and script Creation is {} ", e, s"ctx._source.$e")
      s"ctx._source.$e"
    }).mkString(";")
    log.info("Final Script is {} ", script)
    val request = UpdateRequest(UpdateScript(script, params))
    updateIndex(id, request, Some(seq))
  }
}
