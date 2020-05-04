package io.abp.users.services.users.interpreters

import io.abp.users.domain.User
import io.abp.users.effects.idGenerator._
import io.abp.users.services.users.UserService
import io.abp.users.services.users.UserService.Error._
import zio._
import zio.clock._

object Live {
  def interpreter(usersRef: Ref[Map[User.Id, User]]) =
    new UserService[ZIO] {
      type Env = IdGenerator with Clock

      final def all: IO[GetError, List[User]] =
        for {
          users <- usersRef.get
          result <- IO.succeed(users.values.toList)
        } yield result

      final def get(id: User.Id): IO[GetError, Option[User]] =
        for {
          users <- usersRef.get
          result <- IO.succeed(users.get(id))
        } yield result

      final def getByName(name: String): IO[GetByNameError, List[User]] =
        for {
          users <- usersRef.get
          result <- IO.succeed(users.collect {
            case (_, user) if user.name.equals(name) => user
          }.toList)
        } yield result

      final def create(name: String): ZIO[Env, CreateError, User] =
        for {
          id <- userId
          //TODO: use UTC instead of system timezone
          createdAt <- currentDateTime.mapError(CreateError.TechnicalError)
          user <- IO.succeed(User(id, name, createdAt))
          _ <- usersRef.update(_ + (id -> user))
        } yield user
    }
}
