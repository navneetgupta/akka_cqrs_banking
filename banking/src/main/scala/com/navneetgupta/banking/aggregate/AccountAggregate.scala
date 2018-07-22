package com.navneetgupta.banking.aggregate

import akka.actor.Props
import com.navneetgupta.cqrs.shared.aggregate.BaseAggregate
import com.navneetgupta.banking.command._
import java.util.Date
import com.navneetgupta.cqrs.shared.model.ServiceResult
import com.navneetgupta.cqrs.shared.entity.BasePersistentEntity.GetState
import akka.util.Timeout
import com.navneetgupta.cqrs.shared.model.FullResult
import com.navneetgupta.cqrs.shared.model.Failure
import com.navneetgupta.cqrs.shared.model.FailureType
import com.navneetgupta.cqrs.shared.model.ErrorMessage
import scala.math.BigDecimal

object AccountAggregate {
  val Name = "ua"

  case class OpenAccount(iniialBalance: BigDecimal)
  case class CreditAccount(accountId: String, amount: BigDecimal)
  case class DebitAccount(accountId: String, amount: BigDecimal)
  case class CloseAccount(accountId: String)
  case class GetAccountDetails(accountId: String)

  def props = Props[AccountAggregate]

  val accountDoesNotExistOrInProcess = ErrorMessage("user.account.notExist", Some("The Account Doesn't Exist or Still In Process."))
}
class AccountAggregate extends BaseAggregate[AccountFO, Account] {

  import context.dispatcher
  import scala.concurrent.duration._
  import akka.pattern.ask

  override def receive: akka.actor.Actor.Receive = {
    case AccountAggregate.GetAccountDetails(accountId) =>
      forwardCommand(accountId, GetState(accountId))

    case AccountAggregate.OpenAccount(initalBalance) =>
      val uuid = java.util.UUID.randomUUID().toString
      forwardCommand(uuid, OpenAccount(AccountFO(uuid, initalBalance, new Date(), new Date())))

    case AccountAggregate.CreditAccount(accountId, amount) =>
      implicit val timeout = Timeout(5 seconds)
      val stateFut = (entityShardRegion ? GetState(accountId)).mapTo[ServiceResult[AccountFO]]
      val caller = sender()
      stateFut onComplete {
        case util.Success(FullResult(account)) =>
          entityShardRegion.tell(CreditAccount(account.id, amount), caller)

        case _ =>
          caller ! Failure(FailureType.Service, AccountAggregate.accountDoesNotExistOrInProcess)
      }

    case AccountAggregate.DebitAccount(accountId, amount) =>
      implicit val timeout = Timeout(5 seconds)
      val stateFut = (entityShardRegion ? GetState(accountId)).mapTo[ServiceResult[AccountFO]]
      val caller = sender()
      stateFut onComplete {
        case util.Success(FullResult(account)) =>
          entityShardRegion.tell(DebitAccount(account.id, amount), caller)

        case _ =>
          caller ! Failure(FailureType.Service, AccountAggregate.accountDoesNotExistOrInProcess)
      }

    case AccountAggregate.CloseAccount(accountId) =>
      forwardCommand(accountId, CloseAccount(accountId))

  }

  // Members declared in com.navneetgupta.cqrs.shared.aggregate.BaseAggregate
  override def entityProps: akka.actor.Props = Account.props
}
