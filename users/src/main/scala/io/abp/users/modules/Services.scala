package io.abp.users.modules

import io.abp.users.services.users._

object Services {
  def userService = User.tracing(User.logging(User.inMemory()))
}
