package example

import example.config.AppConfig
import example.domain.User
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
    val createUserProgram: String => ZIO[userService.Env with ProgramEnv, ProgramError, User.Id] =
      UserProgram.createUser(userService)
    val getUserProgram
        : User.Id => ZIO[userService.Env with ProgramEnv, ProgramError, Option[User]] =
      UserProgram.getUser(userService)

    val allUsersProgram: () => ZIO[userService.Env with ProgramEnv, ProgramError, List[User]] =
      UserProgram.getAllUsers(userService)

    val program = for {
      _ <- createUserProgram("Alex").span("User Program - create user")
      _ <- createUserProgram("John").span("User Program - create user")
      id <- createUserProgram("Valentin").span("User Program - create user")
      _ <- getUserProgram(id).span("User Program - get user")
      _ <- allUsersProgram()
    } yield ()
    program
      .provideLayer(programs.userProgramEnv)
      .fold(_ => 1, _ => 0)
  }

}
