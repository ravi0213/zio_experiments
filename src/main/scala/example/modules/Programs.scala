package example.modules

import example.effects.log._
import example.programs.UserProgram.ProgramEnv
import zio.clock._
import zio.console._
import zio.Layer
import zio.random._

object Programs {
  val userProgramEnv: Layer[Nothing, ProgramEnv] =
    Clock.live ++ Console.live ++ Random.live ++ (Console.live >>> Logging.live)
}
