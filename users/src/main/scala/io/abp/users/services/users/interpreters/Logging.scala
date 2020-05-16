package io.abp.users.services.users.interpreters

import cats.instances.string.catsStdShowForString
import cats.syntax.show._
import io.abp.users.domain.User
import io.abp.users.services.users.User.Error._
import io.abp.users.services.users.User.Service
import zio._
import zio.logging._

object Logging {
  type WithLogging[Env] = Env with Logging
  def interpreter[Env](underlying: Service[Env]): Service[WithLogging[Env]] = {
    new Service[WithLogging[Env]] {
      final def all: ZIO[WithLogging[Env], AllError, List[User]] =
        underlying.all
          .foldM(
            e => log.throwable(show"Couldn't get all users. error: $e", e) *> ZIO.fail(e),
            users => log.info(s"Successfully got users $users") *> UIO.succeed(users)
          )

      final def get(id: User.Id): ZIO[WithLogging[Env], GetError, Option[User]] =
        underlying
          .get(id)
          .foldM(
            e => log.throwable(show"Couldn't get user with id ${id.value}. error: $e", e) *> ZIO.fail(e),
            user => log.info(s"Successfully got user $user") *> UIO.succeed(user)
          )

      final def getByName(name: String): ZIO[WithLogging[Env], GetByNameError, List[User]] =
        underlying
          .getByName(name)
          .foldM(
            e => log.throwable(show"Couldn't get user with name $name. error: $e", e) *> ZIO.fail(e),
            user => log.info(s"Successfully got user $user") *> UIO.succeed(user)
          )

      final def create(
          name: String
      ): ZIO[WithLogging[Env], CreateError, User] =
        underlying
          .create(name)
          .foldM(
            e =>
              log.throwable(show"Couldn't create user with name $name. error: $e", e) *> ZIO
                .fail(e),
            u => log.info(s"Successfully created user with id ${u.id}") *> UIO.succeed(u)
          )
    }
  }
}
