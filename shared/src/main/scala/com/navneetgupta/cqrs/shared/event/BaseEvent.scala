package com.navneetgupta.cqrs.shared.event

import com.navneetgupta.cqrs.shared.adapter.DatamodelWriter

trait BaseEvent extends Serializable with DatamodelWriter {
  def entityType: String
}
