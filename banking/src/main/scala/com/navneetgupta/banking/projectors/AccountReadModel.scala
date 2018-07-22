package com.navneetgupta.banking.projectors

import com.navneetgupta.banking.aggregate.Account
import java.util.Date
import com.navneetgupta.cqrs.shared.view.ReadModelObject
import com.navneetgupta.cqrs.shared.view.ViewBuilder
import com.navneetgupta.banking.protocols.AccountJsonProtocol
import akka.actor.Props
import akka.persistence.query.EventEnvelope
import com.navneetgupta.banking.events._
import scala.math.BigDecimal

trait AccountReadModel {
  def indexRoot = "accountsrm"
  def entityType = Account.EntityType
}
object AccountViewBuilder {
  val Name = "account-view-builder"
  case class AccountRM(accountId: String, balance: BigDecimal, createTs: Date, modifyTs: Date, closed: Boolean = false) extends ReadModelObject {
    override def id = accountId
  }

  def props = Props[AccountViewBuilder]
}

class AccountViewBuilder extends ViewBuilder[AccountViewBuilder.AccountRM] with AccountReadModel with AccountJsonProtocol {
  import ViewBuilder._
  import AccountViewBuilder._

  implicit override val rmFormats = accountRMFormat

  def projectionId: String = Name

  def actionFor(id: String, eventEnv: EventEnvelope): ViewBuilder.IndexAction = eventEnv.event match {
    case AccountOpened(account) =>
      val rm = AccountRM(account.id, account.balance, account.createdTs, account.modifyTs, account.closed)
      InsertAction(id, rm)

    case AccountCredited(amount: BigDecimal, creditedOn) =>
      UpdateAction(id, "balance += params.amount", Map("amount" -> amount))
    case AccountDebited(amount: BigDecimal, debitedOn) =>
      UpdateAction(id, "balance -= params.amount", Map("amount" -> amount))
    case AccountClosed(accountId, closedOn) =>
      UpdateAction(id, "closed = params.closed", Map("closed" -> true))
    case evt =>
      NoAction(id)
  }
}
