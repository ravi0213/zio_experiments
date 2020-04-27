package example.services.users.interpreters

import java.time.OffsetDateTime
import scala.collection.mutable.Map

import example.domain.User
import example.services.users.UserService
import example.services.users.UserService.Error._
import zio._

object Live {
  val interpreter = new UserService[ZIO] {
    type Env = Any

    private val users = Map.empty[Long, User]

    final def get(id: Long): IO[GetError, Option[User]] =
      IO.succeed(users.get(id))

    final def create(id: Long, name: String, createdAt: OffsetDateTime): IO[CreateError, Unit] =
      IO.succeed(users.+=((id, User(id, name, createdAt)))) *> IO.unit
  }
}
