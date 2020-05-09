package io.abp.users.services.users.interpreters

import io.abp.users.domain.User
import io.abp.users.effects.idGenerator._
import io.abp.users.services.users._
import io.abp.users.services.users.User.Error._
import io.abp.users.services.users.User.Service
import zio._
import zio.clock._

object InMemory {
  def interpreter(input: Map[User.Id, User]) =
    new Service {
      type Env = IdGenerator with Clock

      private var users: Map[User.Id, User] = input

      final def all: IO[GetError, List[User]] =
        for {
          result <- IO.succeed(users.values.toList)
        } yield result

      final def get(id: User.Id): IO[GetError, Option[User]] =
        for {
          result <- IO.succeed(users.get(id))
        } yield result

      final def getByName(name: String): IO[GetByNameError, List[User]] =
        for {
          result <- IO.succeed(users.collect {
            case (_, user) if user.name.equals(name) => user
          }.toList)
        } yield result

      final def create(name: String): ZIO[Env, CreateError, User] =
        for {
          id <- userId
          //TODO: use UTC instead of system timezone
          createdAt <- currentDateTime.mapError(CreateError.TechnicalError)
          user <- IO.succeed[User](User(id, name, createdAt))
        } yield {
          users = users + (id -> user)
          user
        }
    }
}
