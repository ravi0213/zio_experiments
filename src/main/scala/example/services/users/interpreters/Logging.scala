package example.services.users.interpreters

import java.time.OffsetDateTime

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
          id: Long
      ): ZIO[Env, GetError, Option[User]] =
        underlying
          .get(id)
          .foldM(
            e => error(s"Couldn't get user with id $id. error: $e") *> ZIO.fail(e),
            user => debug(s"Successfully got user $user") *> UIO.succeed(user)
          )

      final def create(
          id: Long,
          name: String,
          createdAt: OffsetDateTime
      ): ZIO[Env, CreateError, Unit] =
        underlying
          .create(id, name, createdAt)
          .foldM(
            e =>
              error(s"Couldn't create user with id $id and name $name. error: $e") *> ZIO.fail(e),
            u => debug(s"Successfully created user with id $id") *> UIO.succeed(u)
          )
    }
}
