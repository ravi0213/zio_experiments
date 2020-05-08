package io.abp.users.services

import io.abp.users.domain.{User => DUser}
import io.abp.users.effects.idGenerator.IdGenerator
import io.abp.users.effects.log.{Logging => LoggingEffect}
import users.User.Error._
import zio._
import zio.clock.Clock
import zio.telemetry.opentracing.OpenTracing

package object users {
  type UserService = Has[User.Service]
  type Env = UserService with IdGenerator with Clock with LoggingEffect with OpenTracing

  object User {
    trait Service extends Serializable {
      def all: ZIO[Env, GetError, List[DUser]]
      def get(id: DUser.Id): ZIO[Env, GetError, Option[DUser]]
      def getByName(name: String): ZIO[Env, GetByNameError, List[DUser]]
      def create(name: String): ZIO[Env, CreateError, DUser]
    }
    sealed trait Error extends Throwable
    object Error {
      sealed trait GetError extends Error
      object GetError {
        case class TechnicalError(cause: Throwable) extends GetError
      }
      sealed trait GetByNameError extends Error
      object GetByNameError {
        case class TechnicalError(cause: Throwable) extends GetByNameError
      }
      sealed trait CreateError extends Error
      object CreateError {
        case class TechnicalError(cause: Throwable) extends CreateError
      }
    }

    import interpreters._

    def live(users: Map[DUser.Id, DUser] = Map.empty): ZLayer[Any, Nothing, UserService] =
      ZLayer.fromEffect(Ref.make(users).map(Live.interpreter))

    def inMemory(): ZLayer[Any, Nothing, UserService] =
      ZLayer.succeed(InMemory.interpreter)

    def logging(underlying: Layer[Nothing, UserService]): ZLayer[Any, Nothing, UserService] =
      (underlying ++ LoggingEffect.consoleLogger) >>> ZLayer.succeed(Logging.interpreter)

    def tracing(underlying: Layer[Nothing, UserService]): ZLayer[Any, Nothing, UserService] =
      (underlying ++ LoggingEffect.consoleLogger) >>> ZLayer.succeed(Logging.interpreter)
  }
  import User.Error._

  def getUser(id: DUser.Id): ZIO[Env, GetError, Option[DUser]] =
    ZIO.accessM(_.get.get(id))

  def getUsersByName(name: String): ZIO[Env, GetByNameError, List[DUser]] =
    ZIO.accessM(_.get.getByName(name))

  def createUser(name: String): ZIO[Env, CreateError, DUser] =
    ZIO.accessM(_.get.create(name))

  def allUsers(): ZIO[Env, GetError, List[DUser]] =
    ZIO.accessM(_.get.all)
}
