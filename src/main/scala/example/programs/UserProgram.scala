package example.programs

import java.io.IOException
import java.time.DateTimeException

import example.services.user._
import zio._
import zio.clock._
import zio.console._
import zio.random._

object UserProgram {
  type ProgramEnv = Clock with Console with Random

  trait ProgramError
  object ProgramError {
    case class ConsoleError(underlying: IOException) extends ProgramError
    case class ClockError(underlying: DateTimeException) extends ProgramError
    case class UserError(underlying: UserService.Error) extends ProgramError
  }

  def createUser(
      userService: UserService.Service[IO]
  ): ZIO[ProgramEnv, ProgramError, Unit] =
    for {
      _ <- putStrLn("Hello! What is your name?")
      name <- getStrLn.mapError(ProgramError.ConsoleError)
      id <- nextLong(1000000000)
      _ <- userService.create(id, name).mapError(ProgramError.UserError)
      dateTime <- currentDateTime.mapError(ProgramError.ClockError)
      user <- userService.get(id).mapError(ProgramError.UserError)
      _ <- putStrLn(
        s"Hello, ${user.map(u => s"${u.name} (id: ${u.id})").getOrElse("Couldn't retrive user")}, welcome to ZIO! It's ${dateTime}"
      )
    } yield ()
}
