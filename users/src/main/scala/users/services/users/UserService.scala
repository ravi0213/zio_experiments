package users.services.users

import users.domain.User
import UserService.Error._

trait UserService[F[_, _, _]] extends Serializable {
  type Env
  def all: F[Env, GetError, List[User]]
  def get(id: User.Id): F[Env, GetError, Option[User]]
  def getByName(name: String): F[Env, GetByNameError, List[User]]
  def create(name: String): F[Env, CreateError, User]
}
object UserService {
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
  import zio._
  def live(users: Map[User.Id, User] = Map.empty) =
    Ref.make(users).map(Live.interpreter)
  def logging(underlying: UserService[zio.ZIO]) = Logging.interpreter(underlying)
  def tracing(underlying: UserService[zio.ZIO]) = Tracing.interpreter(underlying)
}
