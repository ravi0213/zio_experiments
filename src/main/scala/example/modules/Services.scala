package example.modules

import example.services.user._

object Services {
  val userService = UserService.Service.live
}
