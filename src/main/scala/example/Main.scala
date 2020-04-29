package example

import java.time.Instant

import example.config.AppConfig
import example.domain.User
import example.modules.Programs
import example.modules.Services._
import example.programs.UserProgram
import example.programs.UserProgram.{ProgramEnv, ProgramError}
import zio._
import zio.clock._
import zio.console._
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

    val usersCreatedBeforeProgram
        : (Instant) => ZIO[userService.Env with ProgramEnv, ProgramError, List[User]] =
      UserProgram.getUsersCreatedBefore(userService)

    val program = for {
      alexId <- createUserProgram("Alex").span("User Program - create user")
      alex <- getUserProgram(alexId).span("User Program - get user")
      _ <- putStrLn(s"Alex was created at ${alex.get.createdAt}")
      johnId <- createUserProgram("John").span("User Program - create user")
      john <- getUserProgram(johnId).span("User Program - get user")
      _ <- putStrLn(s"John was created at ${john.get.createdAt}")
      valentinId <- createUserProgram("Valentin").span("User Program - create user")
      valentin <- getUserProgram(valentinId).span("User Program - get user")
      _ <- putStrLn(s"Valentin was created at ${valentin.get.createdAt}")
      _ <- allUsersProgram()
      time <- currentDateTime.mapError(ProgramError.ClockError)
      users <- usersCreatedBeforeProgram(time.toInstant.minusMillis(15L))
      _ <- putStrLn(s"Got users ${users.map(_.name)}")
    } yield ()
    program
      .provideLayer(programs.userProgramEnv)
      .fold(_ => 1, _ => 0)
  }

}
