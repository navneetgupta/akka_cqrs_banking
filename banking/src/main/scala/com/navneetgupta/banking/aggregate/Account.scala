package com.navneetgupta.banking.aggregate

import java.util.UUID
import java.util.Date
import akka.actor.Props
import com.navneetgupta.cqrs.shared.entity.BasePersistentEntity
import com.navneetgupta.cqrs.shared.fo.BaseFieldsObject
import com.navneetgupta.banking.command._
import com.navneetgupta.banking.events._
import com.navneetgupta.cqrs.shared.event.BaseEvent
import scala.math.BigDecimal

object AccountFO {
  def empty = AccountFO("", 0.0, new Date(0), new Date(0))
}
case class AccountFO(uuid: String, balance: BigDecimal, createdTs: Date, modifyTs: Date, closed: Boolean = false) extends BaseFieldsObject[String, AccountFO] {
  override def assignId(id: String) = this.copy(uuid = id)
  override def id = uuid;
  override def deleted = closed
  override def markDeleted = this.copy(closed = true)
}

object Account {
  val EntityType = "account"
  def props = Props[Account]
}

class Account extends BasePersistentEntity[AccountFO] {

  def initialState = AccountFO.empty

  override def additionalCommandHandling = {
    case OpenAccount(account) =>
      persist(AccountOpened(account)) { handleEventAndRespond() }
    case DebitAccount(id: String, amount: BigDecimal) =>
      if (state.balance < amount) {
        log.warning("Insufficient Balance to Withdraw from account id: {} with balance : {} asked to withdraw {}", state.id, state.balance, amount)
        sender() ! stateResponse()
      } else {
        persist(AccountDebited(amount, new Date()))(handleEventAndRespond(true))
      }
    case CreditAccount(id: String, amount: BigDecimal) =>
      persist(AccountCredited(amount, new Date()))(handleEventAndRespond(true))
    case CloseAccount(id: String) =>
      persist(AccountClosed(id, new Date()))(handleEventAndRespond(false))
  }

  def handleEvent(event: BaseEvent) = event match {
    case AccountOpened(account) =>
      state = account
    case AccountDebited(amount, occuredOn) =>
      state = state.copy(balance = state.balance - amount, modifyTs = new Date())
    case AccountCredited(amount, occuredOn) =>
      state = state.copy(
        balance = state.balance + amount,
        modifyTs = new Date())
    case AccountClosed(id, closedOn) =>
      state = state.markDeleted
  }

  def isCreateMessage(cmd: Any): Boolean = cmd match {
    case opA: OpenAccount => true
    case _                => false
  }

  override def newDeleteEvent = {
    log.info("===========================================new Delete Event===========")
    log.info("id is : {}", id)
    Some(AccountClosed(id, new Date()))
  }

  override def snapshotAfterCount = Some(5)
}
