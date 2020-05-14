package io.abp.users.services.users.interpreters

import io.abp.users.domain.User
import io.abp.users.services.users.User.Error._
import io.abp.users.services.users.User.Service
import zio._
import zio.logging._

object Logging {
  def interpreter[Env](underlying: Service[Env]): Service[Env with Logging] = {
    type WithLogging = Env with Logging
    new Service[WithLogging] {
      final def all: ZIO[WithLogging, AllError, List[User]] =
        underlying.all
          .foldM(
            e => log.error(s"Couldn't get all users. error: $e", Cause.fail(e)) *> ZIO.fail(e),
            users => log.info(s"Successfully got users $users") *> UIO.succeed(users)
          )

      final def get(id: User.Id): ZIO[WithLogging, GetError, Option[User]] =
        underlying
          .get(id)
          .foldM(
            e => log.error(s"Couldn't get user with id $id. error: $e", Cause.fail(e)) *> ZIO.fail(e),
            user => log.info(s"Successfully got user $user") *> UIO.succeed(user)
          )

      final def getByName(name: String): ZIO[WithLogging, GetByNameError, List[User]] =
        underlying
          .getByName(name)
          .foldM(
            e => log.error(s"Couldn't get user with name $name. error: $e", Cause.fail(e)) *> ZIO.fail(e),
            user => log.info(s"Successfully got user $user") *> UIO.succeed(user)
          )

      final def create(
          name: String
      ): ZIO[WithLogging, CreateError, User] =
        underlying
          .create(name)
          .foldM(
            e =>
              log.error(s"Couldn't create user with name $name. error: $e", Cause.fail(e)) *> ZIO
                .fail(e),
            u => log.info(s"Successfully created user with id ${u.id}") *> UIO.succeed(u)
          )
    }
  }
}
