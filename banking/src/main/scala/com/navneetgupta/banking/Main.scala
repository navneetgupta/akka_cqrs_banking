package com.navneetgupta.banking

import com.navneetgupta.cqrs.shared.server.Server

object Main extends App {
  new Server(new AccountBoot(), "account-management")
}
