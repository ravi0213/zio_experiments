package io.abp.users.modules

import io.abp.users.domain.{User => DUser}
import io.abp.users.effects.idGenerator.IdGenerator
import io.abp.users.services.users._
import zio._
import zio.clock.Clock
import zio.logging.Logging

object Services {
  def userService[Env: Tagged](envs: Environments, usersRef: Ref[Map[DUser.Id, DUser]]) =
    User.inMemory(envs, usersRef) >>> User.logging[IdGenerator with Clock] >>> User
      .tracing[IdGenerator with Clock with Logging]
}
