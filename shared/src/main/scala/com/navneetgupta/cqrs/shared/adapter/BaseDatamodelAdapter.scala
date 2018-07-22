package com.navneetgupta.cqrs.shared.adapter

import akka.persistence.journal.EventAdapter
import scalapb.Message
import akka.persistence.journal.{ EventSeq, Tagged }
import com.navneetgupta.cqrs.shared.event.BaseEvent
import scalapb.GeneratedMessage

trait DatamodelWriter {
  def toDatamodel: GeneratedMessage
}
trait DatamodelReader {
  def fromDatamodel: PartialFunction[GeneratedMessage, AnyRef]
}

class BaseDatamodelAdapter extends EventAdapter {
  override def fromJournal(event: Any, manifest: String): EventSeq = {
    event match {
      case m: GeneratedMessage =>
        val reader = Class.forName(manifest + "$").getField("MODULE$").get(null).asInstanceOf[DatamodelReader]
        reader.
          fromDatamodel.
          lift(m).
          map(EventSeq.single).
          getOrElse(throw readException(event))

      case _ => throw readException(event)
    }
  }

  // Members declared in akka.persistence.journal.WriteEventAdapter
  override def manifest(event: Any): String = {
    event.getClass.getName
  }

  override def toJournal(event: Any): Any = event match {
    case ev: BaseEvent with DatamodelWriter =>
      val message = ev.toDatamodel
      val eventType = ev.getClass.getName.toLowerCase().split("\\$").last
      Tagged(message, Set(ev.entityType, eventType))
    case _ => throw new RuntimeException(s"Protobuf adapter can't write adapt type: $event")
  }

  private def readException(event: Any) = new RuntimeException(s"Protobuf adapter can't read adapt for type: $event")
}
