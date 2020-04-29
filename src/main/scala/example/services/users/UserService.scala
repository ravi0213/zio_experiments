package example.services.users

import example.domain.User
import UserService.Error._

trait UserService[F[_, _, _]] extends Serializable {
  type Env
  def get(id: User.Id): F[Env, GetError, Option[User]]
  def create(name: String): F[Env, CreateError, User]
}
object UserService {
  sealed trait Error extends Throwable
  object Error {
    sealed trait GetError extends Error
    object GetError {
      case class TechnicalError(cause: Throwable) extends GetError
    }
    sealed trait CreateError extends Error
    object CreateError {
      case class TechnicalError(cause: Throwable) extends CreateError
    }
  }

  import interpreters._
  def live = Live.interpreter
  def logging(underlying: UserService[zio.ZIO]) = Logging.interpreter(underlying)
  def tracing(underlying: UserService[zio.ZIO]) = Tracing.interpreter(underlying)
}
