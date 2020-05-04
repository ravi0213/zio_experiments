package users.modules

import users.services.users._

object Services {
  val userService = UserService.live().map(live => UserService.tracing(UserService.logging(live)))
}
