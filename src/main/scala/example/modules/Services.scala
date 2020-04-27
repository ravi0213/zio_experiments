package example.modules

import example.services._

object Services {
  val userService = UserService.logging(UserService.live)
}
