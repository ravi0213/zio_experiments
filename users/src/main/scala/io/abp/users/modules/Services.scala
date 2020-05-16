package io.abp.users.modules

import io.abp.users.config.AppConfig
import io.abp.users.domain.{User => DUser}
import io.abp.users.services.users._
import zio.Ref

object Services {
  def userService(usersRef: Ref[Map[DUser.Id, DUser]], config: AppConfig) =
    User.tracing(User.logging(User.inMemory(usersRef)), config)
}
