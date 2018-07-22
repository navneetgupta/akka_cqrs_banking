package com.navneetgupta.cqrs.shared.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import java.util.Date
import scala.math.BigDecimal

/**
 * Root json protocol class for others to extend from
 */
trait BaseJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object DateFormat extends JsonFormat[Date] {
    override def write(date: Date): JsValue = JsNumber(date.getTime)
    override def read(json: JsValue): Date = json match {
      case JsNumber(epoch) => new Date(epoch.toLong)
      case unknown         => deserializationError(s"Expected JsString, got $unknown")
    }
  }

  implicit object BigIntegerFormat extends JsonFormat[BigDecimal] {
    override def write(value: BigDecimal) = {
      require(value ne null)
      JsNumber(value)
    }
    override def read(json: JsValue) = json match {
      case JsNumber(x) => x
      case JsString(x) => BigDecimal(x)
      case unknown     => deserializationError(s"Expected JsString, got $unknown")
    }
  }

  implicit object AnyJsonFormat extends JsonFormat[Any] {
    override def write(x: Any) = x match {
      case n: Int                   => JsNumber(n)
      case n: BigDecimal            => JsNumber(n)
      case s: String                => JsString(s)
      case b: Boolean if b == true  => JsTrue
      case b: Boolean if b == false => JsFalse
    }
    override def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case JsTrue      => true
      case JsFalse     => false
    }
  }
}
