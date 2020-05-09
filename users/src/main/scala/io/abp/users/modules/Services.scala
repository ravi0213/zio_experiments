package io.abp.users.modules

import io.abp.users.services.users._

object Services {
  val userService = User.inMemory() //User.tracing(User.logging(User.inMemory()))
}
