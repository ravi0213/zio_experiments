package io.abp.users.modules

import io.abp.users.effects.idGenerator.IdGenerator
import io.abp.users.effects.log.Logging
import io.abp.users.services.users._
import zio.clock.Clock

object Services {
  def userService = User.tracing[IdGenerator with Clock with Logging](User.logging[IdGenerator with Clock](User.inMemory()))
}
