package io.abp.users.services

import cats.instances.string.catsStdShowForString
import cats.Show
import cats.syntax.show._
import io.abp.users.config.AppConfig
import io.abp.users.domain.{User => DUser}
import io.abp.users.effects.idGenerator.IdGenerator
import users.User.Error._
import zio._
import zio.clock.Clock
import zio.logging._
import zio.telemetry.opentracing.OpenTracing

package object users {
  type UserService[Env] = Has[User.Service[Env]]

  object User {
    trait Service[Env] extends Serializable {
      def all: ZIO[Env, AllError, List[DUser]]
      def get(id: DUser.Id): ZIO[Env, GetError, Option[DUser]]
      def getByName(name: String): ZIO[Env, GetByNameError, List[DUser]]
      def create(name: String): ZIO[Env, CreateError, DUser]
    }
    sealed trait Error extends Throwable
    object Error {
      sealed trait AllError extends Error
      object AllError {
        case class TechnicalError(description: String, cause: Option[Throwable] = None)
            extends Exception(description, cause.orNull)
            with AllError
      }
      sealed trait GetError extends Error
      object GetError {
        case class TechnicalError(description: String, cause: Option[Throwable] = None)
            extends Exception(description, cause.orNull)
            with GetError
      }
      sealed trait GetByNameError extends Error
      object GetByNameError {
        case class TechnicalError(description: String, cause: Option[Throwable] = None)
            extends Exception(description, cause.orNull)
            with GetByNameError
      }
      sealed trait CreateError extends Error
      object CreateError {
        case class TechnicalError(description: String, cause: Option[Throwable] = None)
            extends Exception(description, cause.orNull)
            with CreateError
      }

      implicit val show: Show[Error] = Show.show {
        case AllError.TechnicalError(description, _) => show"AllError.TechnicalError: $description"
        case GetError.TechnicalError(description, _) => show"GetError.TechnicalError: $description"
        case GetByNameError.TechnicalError(description, _) =>
          show"GetByNameError.TechnicalError: $description"
        case CreateError.TechnicalError(description, _) => show"CreateError.TechnicalError: $description"
      }
    }

    import interpreters._

    def inMemory(users: Ref[Map[DUser.Id, DUser]]): Service[IdGenerator with Clock] =
      InMemory.interpreter(users)
    def logging[Env](underlying: User.Service[Env]): Service[Env with Logging] =
      Logging.interpreter[Env](underlying)
    def tracing[Env](underlying: User.Service[Env], config: AppConfig): Service[Env with OpenTracing] =
      Tracing.interpreter[Env](underlying, config)
  }

  import User.Error._

  def getUser[Env: Tag](id: DUser.Id): ZIO[Env with UserService[Env], GetError, Option[DUser]] =
    ZIO.accessM(_.get.get(id))

  def getUsersByName[Env: Tag](name: String): ZIO[Env with UserService[Env], GetByNameError, List[DUser]] =
    ZIO.accessM(_.get.getByName(name))

  def createUser[Env: Tag](name: String): ZIO[Env with UserService[Env], CreateError, DUser] =
    ZIO.accessM(_.get.create(name))

  def allUsers[Env: Tag](): ZIO[Env with UserService[Env], AllError, List[DUser]] =
    ZIO.accessM(_.get.all)
}
