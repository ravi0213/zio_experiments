package io.abp.users.services.users.interpreters

import io.abp.users.domain.User
import io.abp.users.effects.idGenerator._
import io.abp.users.services.users.User.Error._
import io.abp.users.services.users.User.Service
import zio._
import zio.clock._

object InMemory {
  def interpreter(input: Map[User.Id, User]) = {
    type WithIdAndClock = IdGenerator with Clock
    new Service[WithIdAndClock] {
      private var users: Map[User.Id, User] = input

      final def all: ZIO[WithIdAndClock, GetError, List[User]] =
        for {
          result <- IO.succeed(users.values.toList)
        } yield result

      final def get(id: User.Id): ZIO[WithIdAndClock, GetError, Option[User]] =
        for {
          result <- IO.succeed(users.get(id))
        } yield result

      final def getByName(name: String): ZIO[WithIdAndClock, GetByNameError, List[User]] =
        for {
          result <- IO.succeed(users.collect {
            case (_, user) if user.name.equals(name) => user
          }.toList)
        } yield result

      final def create(name: String): ZIO[WithIdAndClock, CreateError, User] =
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
}
