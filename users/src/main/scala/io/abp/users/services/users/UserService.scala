package io.abp.users.services

import io.abp.users.domain.{User => DUser}
import users.User.Error._
import zio._

package object users {
  object User {
    trait Service[Env] extends Serializable {
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

    def inMemory(input: Map[DUser.Id, DUser] = Map()) = InMemory.interpreter(input)
    def live[Env](users: Map[DUser.Id, DUser] = Map.empty) = Ref.make(users).map(Live.interpreter[Env])
    def logging[Env](underlying: User.Service[Env]) = Logging.interpreter(underlying)
    def tracing[Env](underlying: User.Service[Env]) = Tracing.interpreter(underlying)
  }
}
