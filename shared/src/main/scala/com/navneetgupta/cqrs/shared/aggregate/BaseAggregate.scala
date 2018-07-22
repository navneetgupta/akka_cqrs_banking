package com.navneetgupta.cqrs.shared.aggregate

import com.navneetgupta.cqrs.shared.fo.BaseFieldsObject
import com.navneetgupta.cqrs.shared.actor.BaseActor
import com.navneetgupta.cqrs.shared.entity.BasePersistentEntity

import scala.reflect.ClassTag
import akka.cluster.sharding.ClusterSharding
import akka.cluster.sharding.ClusterShardingSettings
import akka.actor.Props

abstract class BaseAggregate[FO <: BaseFieldsObject[String, FO], E <: BasePersistentEntity[FO]: ClassTag] extends BaseActor {

  val idExtractor = ClusterShardEntityLocator(context.system)

  val entityShardRegion =
    ClusterSharding(context.system).start(
      typeName = entityName,
      entityProps = entityProps,
      settings = ClusterShardingSettings(context.system),
      extractEntityId = idExtractor.extractEntityId,
      extractShardId = idExtractor.extractShardId)

  def forwardCommand(id: String, msg: Any): Unit = {
    entityShardRegion.forward(msg)
  }

  def entityProps: Props

  private def entityName = {
    val entityTag = implicitly[ClassTag[E]]
    entityTag.runtimeClass.getSimpleName()
  }
}
