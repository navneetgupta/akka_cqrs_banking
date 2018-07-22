package com.navneetgupta.banking

import com.navneetgupta.cqrs.shared.boot.Bootstrap
import akka.actor.ActorSystem
import com.navneetgupta.banking.aggregate.AccountAggregate
import com.navneetgupta.banking.projectors.AccountViewBuilder
import com.navneetgupta.banking.routes.AccountRoutes

class AccountBoot extends Bootstrap {
  override def bootstrap(system: ActorSystem) = {
    import system.dispatcher
    val aggregate = system.actorOf(AccountAggregate.props, AccountAggregate.Name)
    startSingleton(system, AccountViewBuilder.props, AccountViewBuilder.Name)
    List(new AccountRoutes(aggregate))
  }
}
