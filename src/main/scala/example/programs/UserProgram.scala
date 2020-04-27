package example.programs

import java.io.IOException
import java.time.DateTimeException

import example.effects.log._
import example.services.UserService
import zio._
import zio.clock._
import zio.console._
import zio.random._

object UserProgram {
  type ProgramEnv = Clock with Console with Random with Logging

  trait ProgramError
  object ProgramError {
    case class ConsoleError(underlying: IOException) extends ProgramError
    case class ClockError(underlying: DateTimeException) extends ProgramError
    case class UserError(underlying: UserService.Error) extends ProgramError
  }

  def createUser(
      userService: UserService[ZIO]
  ): ZIO[userService.Env with ProgramEnv, ProgramError, Unit] =
    for {
      _ <- putStrLn("Hello! What is your name?")
      name <- getStrLn.mapError(ProgramError.ConsoleError)
      id <- nextLong(1000000000)
      dateTime <- currentDateTime.mapError(
        ProgramError.ClockError
      ) //TODO: use UTC instead of system timezone
      _ <- userService.create(id, name, dateTime).mapError(ProgramError.UserError)
      user <- userService.get(id).mapError(ProgramError.UserError)
      _ <- putStrLn(
        s"Hello, ${user.map(u => s"${u.name} (id: ${u.id})").getOrElse("Couldn't retrive user")}, welcome to ZIO!"
      )
    } yield ()
}
