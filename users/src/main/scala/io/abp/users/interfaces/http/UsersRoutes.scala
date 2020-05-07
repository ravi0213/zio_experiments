package io.abp.users.interfaces.http

import io.abp.users.domain.User
import io.abp.users.modules.Environments
import io.abp.users.programs.UserProgram
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import UsersRoutes._
import zio._
import zio.interop.catz._

class UsersRoutes(
    envs: Environments
) extends Http4sDsl[AppTask] {
  private val pathPrefix = Root / "users"
  val routes = HttpRoutes.of[AppTask] {
    case GET -> `pathPrefix` =>
      Ok(
        UserProgram.getAllUsers
        //.mapError({
        //  case ProgramError.UserAlreadyExists => Conflict()
        //  case ProgramError.UserError(_)      => InternalServerError()
        //  case ProgramError.ConsoleError(_)   => InternalServerError()
        //  case ProgramError.ClockError(_)     => InternalServerError()
        //})
          .provideLayer(envs.userProgramEnv)
      )
  }
}

object UsersRoutes {
  def apply(envs: Environments): UsersRoutes = new UsersRoutes(envs)
  type AppTask[A] = ZIO[Any, Throwable, A]
  final case class AllUsersResponse(users: List[User])

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults
  implicit val userIdEncoder: Encoder[User.Id] = deriveConfiguredEncoder[User.Id]
  implicit val userEncoder: Encoder[User] = deriveConfiguredEncoder[User]
  implicit val allUsersRespEncoder: Encoder[AllUsersResponse] = deriveConfiguredEncoder[AllUsersResponse]

  implicit def entityEncoder[A: Encoder]: EntityEncoder[AppTask, A] = jsonEncoderOf[AppTask, A]

}
