package com.navneetgupta.banking.events

import com.navneetgupta.cqrs.shared.event.BaseEvent
import com.navneetgupta.banking.aggregate.Account
import com.navneetgupta.banking.aggregate.AccountFO
import com.navneetgupta.banking.proto.events.{ Account => ProtoAccount, AccountOpened => ProtoAccountOpened, AccountCredited => ProtoAccountCredited, AccountDebited => ProtoAccountDebited, AccountClosed => ProtoAccountClosed }
import java.util.Date
import scala.math.BigDecimal
import com.navneetgupta.cqrs.shared.adapter.DatamodelReader

trait AccountEvent extends BaseEvent { override def entityType = Account.EntityType }

case class AccountOpened(account: AccountFO) extends AccountEvent {
  override def toDatamodel = {
    val accountDM = ProtoAccount.apply(
      id = account.id,
      balance = account.balance.toString(),
      createTs = new Date().getTime(),
      modifyTs = new Date().getTime(),
      closed = account.closed)

    ProtoAccountOpened(Some(accountDM))
  }
}

object AccountOpened extends DatamodelReader {
  override def fromDatamodel = {
    case dm: ProtoAccountOpened =>
      println("From DataModel AccountOpened")
      val account = dm.account.get
      AccountOpened(
        account = AccountFO(account.id, BigDecimal(account.balance), new Date(account.createTs), new Date(account.modifyTs), account.closed))
  }
}

case class AccountCredited(amount: BigDecimal, occuredOn: Date) extends AccountEvent {
  override def toDatamodel = {
    ProtoAccountCredited.apply(amount.toString(), new Date().getTime())
  }
}

object AccountCredited extends DatamodelReader {
  override def fromDatamodel = {
    case dm: ProtoAccountCredited =>
      AccountCredited(BigDecimal(dm.balance), new Date(dm.occuredOn))
  }
}

case class AccountDebited(amount: BigDecimal, occuredOn: Date) extends AccountEvent {
  override def toDatamodel = {
    ProtoAccountDebited.apply(amount.toString(), new Date().getTime())
  }
}

object AccountDebited extends DatamodelReader {
  override def fromDatamodel = {
    case dm: ProtoAccountDebited =>
      AccountDebited.apply(BigDecimal(dm.balance), new Date(dm.occuredOn))
  }
}

case class AccountClosed(accountId: String, closedOn: Date) extends AccountEvent {
  override def toDatamodel = {
    ProtoAccountClosed.apply(accountId, closedOn.getTime())
  }
}

object AccountClosed extends DatamodelReader {
  override def fromDatamodel = {
    case dm: ProtoAccountClosed =>
      AccountClosed(dm.id, new Date(dm.modifyTs))
  }
}
