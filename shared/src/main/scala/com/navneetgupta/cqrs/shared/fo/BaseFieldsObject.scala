package com.navneetgupta.cqrs.shared.fo

trait BaseFieldsObject[K, FO] extends Serializable {
  def assignId(id: K): FO
  def id: K
  def deleted: Boolean
  def markDeleted: FO
}
