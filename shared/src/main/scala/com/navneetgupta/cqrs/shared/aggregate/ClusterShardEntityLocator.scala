package com.navneetgupta.cqrs.shared.aggregate

import akka.actor.ActorSystem
import akka.cluster.sharding.ShardRegion
import com.navneetgupta.cqrs.shared.command.BaseCommand

object ClusterShardEntityLocator {
  def apply(system: ActorSystem): ClusterShardEntityLocator = {
    val maxShards = system.settings.config.getInt("maxShards")
    new ClusterShardEntityLocator(maxShards)(system)
  }
}
class ClusterShardEntityLocator(maxShards: Int)(implicit actorSystem: ActorSystem) {
  val extractEntityId: ShardRegion.ExtractEntityId = {
    case bc: BaseCommand => (bc.entityId, bc)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case bc: BaseCommand =>
      (math.abs(bc.entityId.hashCode) % maxShards).toString
  }
  println("====================ClusterShardEntityLocator========================")
  println("extractEntityId = " + extractEntityId)
  println("extractShardId = " + extractShardId)
  println("====================ClusterShardEntityLocator========================")
}
