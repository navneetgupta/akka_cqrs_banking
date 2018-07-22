package com.navneetgupta.cqrs.shared.command

import com.navneetgupta.cqrs.shared.adapter.DatamodelReader

trait BaseCommand {
  def entityId: String
}
