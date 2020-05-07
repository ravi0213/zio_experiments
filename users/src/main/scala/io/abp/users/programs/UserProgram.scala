package io.abp.users.programs

import java.io.IOException
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneOffset

import io.abp.users.domain.User
import io.abp.users.services.users
import io.abp.users.services.users.{User => UserService, _}
import zio._
import zio.console._
import zio.random._

object UserProgram {
  type ProgramEnv = Console with Random with UserService with Env

  //The existence check wouldn't work in a concurrent system. We need semantic locking.
  //TODO: explore ZIO.STM and ZIO.Ref
  def createUser(name: String): ZIO[ProgramEnv, ProgramError, User.Id] =
    for {
      result <- getUsersByName(name).mapError(ProgramError.UserError)
      user <-
        if (result.isEmpty) users.createUser(name).mapError(ProgramError.UserError)
        else ZIO.fail(ProgramError.UserAlreadyExists)
    } yield user.id

  def getUser(id: User.Id): ZIO[ProgramEnv, ProgramError, Option[User]] =
    users.getUser(id).mapError(ProgramError.UserError)

  def getAllUsers(): ZIO[ProgramEnv, ProgramError, List[User]] =
    allUsers().mapError(ProgramError.UserError)

  def getUsersCreatedBefore(instant: Instant): ZIO[ProgramEnv, ProgramError, List[User]] =
    allUsers()
      .mapError(ProgramError.UserError)
      .map(_.filter(_.createdAt.atZoneSameInstant(ZoneOffset.UTC).toInstant.isBefore(instant)))

  trait ProgramError extends Throwable
  object ProgramError {
    case class ConsoleError(underlying: IOException) extends ProgramError
    case class ClockError(underlying: DateTimeException) extends ProgramError
    case class UserError(underlying: UserService.Error) extends ProgramError
    case object UserAlreadyExists extends ProgramError
  }

}
