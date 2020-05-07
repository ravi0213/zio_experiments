package io.abp.users.services.users.interpreters

import io.abp.users.domain.User
import io.abp.users.effects.log._
import io.abp.users.services.users._
import io.abp.users.services.users.User.Error._
import io.abp.users.services.users.User.Service
import zio._

object Logging {
  val interpreter =
    new Service {
      final def all: ZIO[Env with UserService, GetError, List[User]] =
        allUsers()
          .foldM(
            e => error(e)(s"Couldn't get all users. error: $e") *> ZIO.fail(e),
            users => debug(s"Successfully got users $users") *> UIO.succeed(users)
          )

      final def get(
          id: User.Id
      ): ZIO[Env with UserService, GetError, Option[User]] =
        getUser(id)
          .foldM(
            e => error(e)(s"Couldn't get user with id $id. error: $e") *> ZIO.fail(e),
            user => debug(s"Successfully got user $user") *> UIO.succeed(user)
          )

      final def getByName(name: String): ZIO[Env with UserService, GetByNameError, List[User]] =
        getUsersByName(name)
          .foldM(
            e => error(e)(s"Couldn't get user with name $name. error: $e") *> ZIO.fail(e),
            user => debug(s"Successfully got user $user") *> UIO.succeed(user)
          )

      final def create(
          name: String
      ): ZIO[Env with UserService, CreateError, User] =
        createUser(name)
          .foldM(
            e =>
              error(e)(s"Couldn't create user with name $name. error: $e") *> ZIO
                .fail(e),
            u => debug(s"Successfully created user with id ${u.id}") *> UIO.succeed(u)
          )
    }
}
