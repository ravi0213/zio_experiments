package io.abp.users.modules

import io.abp.users.services.users._

object Services {
  val userService = UserService.live().map(live => UserService.tracing(UserService.logging(live)))
}
