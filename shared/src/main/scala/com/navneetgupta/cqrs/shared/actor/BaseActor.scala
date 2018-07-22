package com.navneetgupta.cqrs.shared.actor

import akka.actor._
import com.navneetgupta.cqrs.shared.model.{ ServiceResult, FailureType, Failure, FullResult }
import scala.concurrent.Future

trait BaseActor extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  //PartialFunction to be used with the .recover combinator to convert an exception on a failed Future into a
  //Failure ServiceResult
  private val toFailure: PartialFunction[Throwable, ServiceResult[Nothing]] = {
    case ex =>
      log.info("Got Failure Exceptions is {}", ex.getMessage)
      ex.printStackTrace()
      Failure(FailureType.Service, ServiceResult.UnexpectedFailure, Some(ex))
  }

  /**
   * Pipes the response from a request to a service actor back to the sender, first
   * converting to a ServiceResult per the contract of communicating with a bookstore service
   * @param f The Future to map the result from into a ServiceResult
   */
  def pipeResponse[T](f: Future[T]): Unit =
    f.
      map {
        case o: Option[_] =>
          log.info("Got Optional Response")
          ServiceResult.fromOption(o)
        case f: Failure =>
          log.info("Got Failure Response")
          f
        case other =>
          log.info("Final Response is {} ", other)
          FullResult(other)
      }.
      recover(toFailure).
      pipeTo(sender())
}
