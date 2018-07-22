package com.navneetgupta.banking.protocols

import com.navneetgupta.cqrs.shared.json.BaseJsonProtocol
import com.navneetgupta.banking.aggregate.AccountFO
import com.navneetgupta.banking.projectors.AccountViewBuilder.AccountRM
import com.navneetgupta.banking.aggregate.AccountAggregate

trait AccountJsonProtocol extends BaseJsonProtocol {
  implicit val accountFoFormat = jsonFormat5(AccountFO.apply)
  implicit val accountRMFormat = jsonFormat5(AccountRM.apply)
  implicit val creditAccountFormat = jsonFormat2(AccountAggregate.CreditAccount.apply)
  implicit val debitAccountFormat = jsonFormat2(AccountAggregate.DebitAccount.apply)
  implicit val openAccountFormat = jsonFormat1(AccountAggregate.OpenAccount.apply)
}
