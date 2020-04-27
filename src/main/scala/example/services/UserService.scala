package example.services

import java.time.OffsetDateTime
import scala.collection.mutable.Map

import example.domain.User
import UserService.Error._
import zio._

trait UserService[F[_, _, _]] extends Serializable {
  type Env
  def get(id: Long): F[Env, GetError, Option[User]]
  def create(id: Long, name: String, createdAt: OffsetDateTime): F[Env, CreateError, Unit]
}
object UserService {

  sealed trait Error
  object Error {
    sealed trait GetError extends Error
    sealed trait CreateError extends Error
  }

  val live = new UserService[ZIO] {
    type Env = Any

    private val users = Map.empty[Long, User]

    final def get(id: Long): IO[GetError, Option[User]] =
      IO.succeed(users.get(id))

    final def create(id: Long, name: String, createdAt: OffsetDateTime): IO[CreateError, Unit] =
      IO.succeed(users.+=((id, User(id, name, createdAt)))) *> IO.unit

  }

  import example.effects.log._
  def logging(underlying: UserService[ZIO]) =
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
