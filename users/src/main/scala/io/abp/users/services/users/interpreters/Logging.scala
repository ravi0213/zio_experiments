package io.abp.users.services.users.interpreters

import io.abp.users.domain.User
import io.abp.users.effects.log._
import io.abp.users.services.users._
import io.abp.users.services.users.User.Error._
import io.abp.users.services.users.User.Service
import zio._

object Logging {
  def interpreter(underlying: Service) =
    new Service {
      type Env = underlying.Env with Logging

      final def all: ZIO[Env, GetError, List[User]] =
        underlying.all
          .foldM(
            e => error(e)(s"Couldn't get all users. error: $e") *> ZIO.fail(e),
            users => debug(s"Successfully got users $users") *> UIO.succeed(users)
          )

      final def get(id: User.Id): ZIO[Env, GetError, Option[User]] =
        underlying
          .get(id)
          .foldM(
            e => error(e)(s"Couldn't get user with id $id. error: $e") *> ZIO.fail(e),
            user => debug(s"Successfully got user $user") *> UIO.succeed(user)
          )

      final def getByName(name: String): ZIO[Env, GetByNameError, List[User]] =
        underlying
          .getByName(name)
          .foldM(
            e => error(e)(s"Couldn't get user with name $name. error: $e") *> ZIO.fail(e),
            user => debug(s"Successfully got user $user") *> UIO.succeed(user)
          )

      final def create(
          name: String
      ): ZIO[Env, CreateError, User] =
        underlying
          .create(name)
          .foldM(
            e =>
              error(e)(s"Couldn't create user with name $name. error: $e") *> ZIO
                .fail(e),
            u => debug(s"Successfully created user with id ${u.id}") *> UIO.succeed(u)
          )
    }
}
