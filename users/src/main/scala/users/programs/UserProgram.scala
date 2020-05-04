package users.programs

import java.io.IOException
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneOffset

import users.domain.User
import users.services.users.UserService
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
    } yield user.id

  def getUser(
      userService: UserService[ZIO]
  )(id: User.Id): ZIO[userService.Env with ProgramEnv, ProgramError, Option[User]] =
    userService.get(id).mapError(ProgramError.UserError)

  def getAllUsers(
      userService: UserService[ZIO]
  )(): ZIO[userService.Env with ProgramEnv, ProgramError, List[User]] =
    userService.all.mapError(ProgramError.UserError)

  def getUsersCreatedBefore(
      userService: UserService[ZIO]
  )(instant: Instant): ZIO[userService.Env with ProgramEnv, ProgramError, List[User]] =
    userService.all
      .mapError(ProgramError.UserError)
      .map(_.filter(_.createdAt.atZoneSameInstant(ZoneOffset.UTC).toInstant.isBefore(instant)))
}
