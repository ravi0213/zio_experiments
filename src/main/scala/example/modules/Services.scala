package example.modules

import example.services.users._

object Services {
  val userService = UserService.logging(UserService.live)
}
