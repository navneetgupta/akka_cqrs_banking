package com.navneetgupta.banking.command

import scala.math.BigDecimal
import com.navneetgupta.banking.aggregate.AccountFO
import com.navneetgupta.cqrs.shared.command.BaseCommand

//User Input
case class AccountInput(initialBalance: BigDecimal)

case class OpenAccount(account: AccountFO) extends BaseCommand {
  override def entityId = account.id
}
case class CreditAccount(id: String, amount: BigDecimal) extends BaseCommand {
  override def entityId = id
}
case class DebitAccount(id: String, amount: BigDecimal) extends BaseCommand {
  override def entityId = id
}
case class CloseAccount(id: String) extends BaseCommand {
  override def entityId = id
}
