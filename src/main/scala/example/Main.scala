package example

import example.config.AppConfig
import example.modules.Programs
import example.modules.Services._
import example.programs.UserProgram
import zio._
import zio.telemetry.opentracing._

object Main extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val config = AppConfig.live
    val programs = new Programs(config)
    UserProgram
      .createUser(userService)
      .root("User Program - Root Span")
      .provideLayer(programs.userProgramEnv)
      .fold(_ => 1, _ => 0)
  }
}
