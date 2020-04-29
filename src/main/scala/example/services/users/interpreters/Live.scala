package example.services.users.interpreters

import scala.collection.mutable.Map

import example.domain.User
import example.effects.idGenerator._
import example.services.users.UserService
import example.services.users.UserService.Error._
import zio._
import zio.clock._

object Live {
  val interpreter = new UserService[ZIO] {
    type Env = IdGenerator with Clock

    private val users = Map.empty[User.Id, User]

    final def get(id: User.Id): IO[GetError, Option[User]] =
      IO.succeed(users.get(id))

    final def create(name: String): ZIO[Env, CreateError, User] =
      for {
        id <- userId
        //TODO: use UTC instead of system timezone
        createdAt <- currentDateTime.mapError(CreateError.TechnicalError)
        user <- IO.succeed(User(id, name, createdAt))
        _ <- IO.succeed(users.+=((id, user)))
      } yield user
  }
}
