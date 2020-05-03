package example.modules

import example.services.users._

object Services {
  val userService = UserService.live().map(live => UserService.tracing(UserService.logging(live)))
}
