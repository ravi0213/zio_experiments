package example.modules

import example.effects.log._
import example.programs.UserProgram.ProgramEnv
import zio.clock._
import zio.console._
import zio.Layer
import zio.random._

object Programs {
  private val defaultEnv = Clock.live ++ Console.live ++ Random.live
  val userProgramEnv: Layer[Nothing, ProgramEnv] = defaultEnv ++ Logging.consoleLogger
}
