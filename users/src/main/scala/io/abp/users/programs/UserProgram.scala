package io.abp.users.programs

import java.io.IOException
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneOffset

import io.abp.users.domain.User
import io.abp.users.services.users
import io.abp.users.services.users._
import zio._

object UserProgram {
  type ProgramEnv[Env] = Env with UserService[Env]

  //The existence check wouldn't work in a concurrent system. We need semantic locking.
  //TODO: explore ZIO.STM and ZIO.Ref
  def createUser[Env: Tag](name: String): ZIO[ProgramEnv[Env], ProgramError, User.Id] =
    (for {
      result <- getUsersByName(name).mapError(ProgramError.UserError)
      user <-
        if (result.isEmpty) users.createUser(name).mapError(ProgramError.UserError)
        else ZIO.fail(ProgramError.UserAlreadyExists)
    } yield user.id)

  def getUser[Env: Tag](id: User.Id): ZIO[ProgramEnv[Env], ProgramError, Option[User]] =
    users
      .getUser(id)
      .mapError(ProgramError.UserError)

  def getAllUsers[Env: Tag](): ZIO[ProgramEnv[Env], ProgramError, List[User]] =
    allUsers
      .mapError(ProgramError.UserError)

  def getUsersCreatedBefore[Env: Tag](
      instant: Instant
  ): ZIO[ProgramEnv[Env], ProgramError, List[User]] =
    allUsers
      .mapError(ProgramError.UserError)
      .map(_.filter(_.createdAt.atZoneSameInstant(ZoneOffset.UTC).toInstant.isBefore(instant)))

  trait ProgramError extends Throwable
  object ProgramError {
    case class ConsoleError(underlying: IOException) extends ProgramError
    case class ClockError(underlying: DateTimeException) extends ProgramError
    case class UserError(underlying: users.User.Error) extends ProgramError
    case object UserAlreadyExists extends ProgramError
  }

}
