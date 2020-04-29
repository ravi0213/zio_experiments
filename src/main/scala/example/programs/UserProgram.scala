package example.programs

import java.io.IOException
import java.time.DateTimeException

import example.services.users.UserService
import zio._
import zio.console._
import zio.random._

object UserProgram {
  type ProgramEnv = Console with Random

  trait ProgramError
  object ProgramError {
    case class ConsoleError(underlying: IOException) extends ProgramError
    case class ClockError(underlying: DateTimeException) extends ProgramError
    case class UserError(underlying: UserService.Error) extends ProgramError
  }

  def createUser(
      userService: UserService[ZIO]
  )(name: String): ZIO[userService.Env with ProgramEnv, ProgramError, Unit] =
    for {
      name <- IO.succeed(name)
      user <- userService.create(name).mapError(ProgramError.UserError)
      _ <- putStrLn(s"Hello, ${user.name}")
    } yield ()
}
