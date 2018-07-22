package com.navneetgupta.cqrs.shared.model

/**
 * Represents an error message from a failure with a service call.  Has fields for the code of the error
 * as well as a description of the error
 */
case class ErrorMessage(code: String, shortText: Option[String] = None, params: Option[Map[String, String]] = None)

/**
 * Companion to ErrorMessage
 */
object ErrorMessage {
  /**
   * Common error where an operation is requested on an entity that does not exist
   */
  val InvalidEntityId = ErrorMessage("invalid.entity.id", Some("No matching entity found"))
}
