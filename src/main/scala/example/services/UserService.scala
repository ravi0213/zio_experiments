package example.services

import scala.collection.mutable.Map

import example.domain.User
import zio._

package object user {

  object UserService extends Serializable {
    sealed trait Error
    object Error {
      sealed trait GetError extends Error
      sealed trait CreateError extends Error
    }

    import Error._

    trait Service[F[_, _]] extends Serializable {
      def get(id: Long): F[GetError, Option[User]]
      def create(id: Long, name: String): F[CreateError, Unit]
    }
    object Service {
      val live: Service[IO] = new Service[IO] {
        private val users = Map.empty[Long, User]

        final def get(id: Long): IO[GetError, Option[User]] =
          IO.succeed(users.get(id))

        final def create(id: Long, name: String): IO[CreateError, Unit] =
          IO.succeed(users.+=((id, User(id, name)))) *> IO.unit

      }
    }
  }
}
