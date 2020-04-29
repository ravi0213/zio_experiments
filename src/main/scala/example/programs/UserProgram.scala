package example.programs

import java.io.IOException
import java.time.DateTimeException

import example.domain.User
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
    case object UserAlreadyExists extends ProgramError
  }

  //The existence check wouldn't work in a concurrent system. We need semantic locking.
  //TODO: explore ZIO.STM and ZIO.Ref
  def createUser(
      userService: UserService[ZIO]
  )(name: String): ZIO[userService.Env with ProgramEnv, ProgramError, User.Id] =
    for {
      users <- userService.getByName(name).mapError(ProgramError.UserError)
      user <- if (users.isEmpty) userService.create(name).mapError(ProgramError.UserError)
      else ZIO.fail(ProgramError.UserAlreadyExists)
      _ <- putStrLn(s"Hello, ${user.name}")
    } yield user.id

  def getUser(
      userService: UserService[ZIO]
  )(id: User.Id): ZIO[userService.Env with ProgramEnv, ProgramError, Option[User]] =
    for {
      user <- userService.get(id).mapError(ProgramError.UserError)
      _ <- putStrLn(user.map(u => s"Hello, ${u.name}").getOrElse("Oh No"))
    } yield user

  def getAllUsers(
      userService: UserService[ZIO]
  )(): ZIO[userService.Env with ProgramEnv, ProgramError, List[User]] =
    for {
      users <- userService.all.mapError(ProgramError.UserError)
      _ <- ZIO.foreach(users)(u => putStrLn(s"Hello, ${u.name}"))
    } yield users
}
