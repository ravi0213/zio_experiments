package example

import example.config.AppConfig
import example.modules.Programs
import example.modules.Services._
import example.programs.UserProgram
import example.programs.UserProgram.{ProgramEnv, ProgramError}
import zio._
import zio.telemetry.opentracing._

object Main extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val config = AppConfig.live
    val programs = new Programs(config)
    val createUserProgram: String => ZIO[userService.Env with ProgramEnv, ProgramError, Unit] =
      UserProgram.createUser(userService)
    val program = (for {
      _ <- createUserProgram("Alex").span("User Program - Root Span")
      _ <- createUserProgram("John").span("User Program - Root Span")
      _ <- createUserProgram("Valentin").span("User Program - Root Span")
    } yield ())
      .provideLayer(programs.userProgramEnv)
    program.fold(_ => 1, _ => 0)
  }

}
