package example.services

import scala.collection.mutable.Map

import example.domain.User
import zio._

package object user {
  object Errors {
    sealed trait UserServiceError
    object UserServiceError {
      sealed trait GetError extends UserServiceError
      sealed trait CreateError extends UserServiceError
    }
  }

  import Errors.UserServiceError._

  import scala.util.Random
  type UserService = Has[UserService.Service[IO]]

  object UserService extends Serializable {
    trait Service[F[_, _]] extends Serializable {
      def get(id: String): F[GetError, Option[User]]
      def create(name: String): F[CreateError, String]
    }
    object Service {
      val live: Service[IO] = new Service[IO] {
        private val users = Map.empty[String, User]

        final def get(id: String): ZIO[Any, GetError, Option[User]] =
          ZIO.effectSuspendTotal[Any, GetError, Option[User]](
            ZIO.succeed(users.get(id))
          )

        final def create(name: String): ZIO[Any, CreateError, String] =
          for {
            id <- ZIO.effectSuspendTotal[Any, CreateError, String](
              ZIO.succeed(Random.nextString(8))
            )
            _ <- ZIO.effectSuspendTotal[Any, CreateError, Map[String, User]](
              ZIO.succeed(users.+=((id, User(id, name))))
            )
          } yield id

      }
    }

    val any: ZLayer[Service[IO], Nothing, Service[IO]] =
      ZLayer.requires[Service[IO]]

    val live: Layer[Nothing, UserService] =
      ZLayer.succeed(Service.live)

  }

  def getUser(id: String): ZIO[UserService, GetError, Option[User]] =
    ZIO.accessM(_.get get id)

  def createUser(name: String): ZIO[UserService, CreateError, String] =
    ZIO.accessM(_.get create name)
}
