package example.modules

import example.effects.log._
import zio.clock._
import zio.console._
import zio.random._

object Programs {
  private val defaultEnv = Clock.live ++ Console.live ++ Random.live
  val userProgramEnv = defaultEnv ++ Logging.consoleLogger
}
