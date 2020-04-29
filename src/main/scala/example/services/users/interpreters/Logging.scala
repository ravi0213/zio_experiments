package example.services.users.interpreters

import example.domain.User
import example.effects.log._
import example.services.users.UserService
import example.services.users.UserService.Error._
import zio._

object Logging {
  def interpreter(underlying: UserService[ZIO]) =
    new UserService[ZIO] {
      type Env = underlying.Env with Logging

      final def get(
          id: User.Id
      ): ZIO[Env, GetError, Option[User]] =
        underlying
          .get(id)
          .foldM(
            e => error(e)(s"Couldn't get user with id $id. error: $e") *> ZIO.fail(e),
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
