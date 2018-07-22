package com.navneetgupta.cqrs.shared.boot

import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.actor.Props
import akka.cluster.singleton.ClusterSingletonManager
import akka.actor.ActorRef
import akka.cluster.singleton.ClusterSingletonManagerSettings
import com.navneetgupta.cqrs.shared.rotues.BaseRouteDefination

/**
 * Trait that defines a class that will boot up actors from within a specific services module
 */
trait Bootstrap {

  /**
   * Books up the actors for a service module and returns the service endpoints for that
   * module to be included in the Unfiltered server as plans
   * @param system The actor system to boot actors into
   * @return a List of BookstorePlans to add as plans into the server
   */
  def bootstrap(system: ActorSystem): List[BaseRouteDefination]

  def startSingleton(system: ActorSystem, props: Props,
                     managerName: String, terminationMessage: Any = PoisonPill): ActorRef = {

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props,
        terminationMessage = terminationMessage,
        settings = ClusterSingletonManagerSettings(system)),
      managerName)
  }
}
