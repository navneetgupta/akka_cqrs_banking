package com.navneetgupta.cqrs.shared.server

import com.navneetgupta.cqrs.shared.boot.Bootstrap
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.scaladsl.Sink

class Server(boot: Bootstrap, service: String) {

  import akka.http.scaladsl.server.Directives._
  val conf = ConfigFactory.load()

  implicit val system = ActorSystem("CQRSApp", conf)
  implicit val mater = ActorMaterializer()
  val log = Logging(system.eventStream, "Server")
  import system.dispatcher

  val routes = boot.bootstrap(system).map(_.routes)
  val definedRoutes = routes.reduce(_ ~ _)
  val finalRoutes = pathPrefix("api")(definedRoutes)

  val conf1 = ConfigFactory.load.getConfig(service).resolve()

  val serviceConf = system.settings.config.getConfig(service)

  val serverSource =
    Http().bind(interface = serviceConf.getString("ip"), port = serviceConf.getInt("port"))

  log.info("Starting up on port {} and ip {}", serviceConf.getString("port"), serviceConf.getString("ip"))

  val sink = Sink.foreach[Http.IncomingConnection](_.handleWith(finalRoutes))
  serverSource.to(sink).run
}
