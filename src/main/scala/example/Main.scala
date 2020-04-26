package example

import java.io.IOException
import java.time.DateTimeException

import example.services.user._
import zio._
import zio.clock._
import zio.console._

object Main extends App {

  type ProgramEnv = Clock with Console with UserService
  object ProgramEnv {
    val any: ZLayer[ProgramEnv, Nothing, ProgramEnv] =
      ZLayer.requires[ProgramEnv]
    val live: Layer[Nothing, ProgramEnv] =
      Clock.live ++ Console.live ++ UserService.live
  }

  trait ProgramError
  object ProgramError {
    case class ConsoleError(underlying: IOException) extends ProgramError
    case class ClockError(underlying: DateTimeException) extends ProgramError
    case class UserError(underlying: Errors.UserServiceError)
        extends ProgramError
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    myAppLogic.provideLayer(ProgramEnv.live).fold(_ => 1, _ => 0)

  val myAppLogic: ZIO[ProgramEnv, ProgramError, Unit] =
    for {
      _ <- putStrLn("Hello! What is your name?")
      name <- getStrLn.mapError(ProgramError.ConsoleError)
      id <- createUser(name).mapError(ProgramError.UserError)
      dateTime <- currentDateTime.mapError(ProgramError.ClockError)
      user <- getUser(id).mapError(ProgramError.UserError)
      _ <- putStrLn(
        s"Hello, ${user.map(_.name).getOrElse("Couldn't retrive user")}, welcome to ZIO! It's ${dateTime}"
      )
    } yield ()
}
