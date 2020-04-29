package example.services.users

import java.time.OffsetDateTime

import example.domain.User
import UserService.Error._

trait UserService[F[_, _, _]] extends Serializable {
  type Env
  def get(id: Long): F[Env, GetError, Option[User]]
  def create(id: Long, name: String, createdAt: OffsetDateTime): F[Env, CreateError, Unit]
}
object UserService {
  sealed trait Error extends Throwable
  object Error {
    sealed trait GetError extends Error
    object GetError {
      case class TechnicalError(cause: Throwable) extends GetError
    }
    sealed trait CreateError extends Error
  }

  import interpreters._
  def live = Live.interpreter
  def logging(underlying: UserService[zio.ZIO]) = Logging.interpreter(underlying)
  def tracing(underlying: UserService[zio.ZIO]) = Tracing.interpreter(underlying)
}
