package com.navneetgupta.banking.routes

import akka.actor._
import com.navneetgupta.cqrs.shared.rotues.BaseRouteDefination
import com.navneetgupta.banking.protocols.AccountJsonProtocol
import scala.concurrent.ExecutionContext
import akka.stream.Materializer
import akka.http.scaladsl.server.Route
import com.navneetgupta.banking.aggregate.AccountFO
import com.navneetgupta.banking.aggregate.AccountAggregate._

class AccountRoutes(accountAggregate: ActorRef) extends BaseRouteDefination with AccountJsonProtocol {

  import akka.pattern._
  import akka.http.scaladsl.server.Directives._

  override def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route = {
    pathPrefix("account") {
      path(Segment) { accountID =>
        get {
          serviceAndComplete[AccountFO](GetAccountDetails(accountID), accountAggregate)
        } ~
          delete {
            serviceAndComplete[AccountFO](CloseAccount(accountID), accountAggregate)
          }
      } ~
        pathPrefix("withdraw") {
          put {
            entity(as[DebitAccount]) { input =>
              serviceAndComplete[AccountFO](input, accountAggregate)
            }
          }
        } ~
        pathPrefix("deposit") {
          put {
            entity(as[CreditAccount]) { input =>
              serviceAndComplete[AccountFO](input, accountAggregate)
            }
          }
        } ~
        pathEndOrSingleSlash {
          post {
            entity(as[OpenAccount]) { input =>
              serviceAndComplete[AccountFO](input, accountAggregate)
            }
          }
        }
    }
  }
}
