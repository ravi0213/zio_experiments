package io.abp.users.interfaces.http

import io.abp.users.domain.User
import io.abp.users.interfaces.http.UsersRoutes._
import io.abp.users.modules.{Environments, Services}
import io.abp.users.programs.UserProgram
import io.abp.users.programs.UserProgram.ProgramError
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import zio._
import zio.interop.catz._

class UsersRoutes(
    envs: Environments,
    usersRef: Ref[Map[User.Id, User]]
) extends Http4sDsl[AppTask] {
  val userService = Services.userService(usersRef)

  private val pathPrefix = Root / "users"
  val routes = HttpRoutes.of[AppTask] {
    case GET -> `pathPrefix` =>
      UserProgram
        .getAllUsers(userService)
        .provideLayer(envs.userProgramEnv)
        .foldM(errorHandler, Ok(_))
    case GET -> `pathPrefix` / id =>
      UserProgram
        .getUser(userService)(User.Id(id))
        .provideLayer(envs.userProgramEnv)
        .foldM(errorHandler, Ok(_))

    case request @ POST -> `pathPrefix` =>
      request.as[CreateUserRequest].flatMap { req =>
        UserProgram
          .createUser(userService)(req.name)
          .provideLayer(envs.userProgramEnv)
          .foldM(errorHandler, Ok(_))
      }
  }

  private def errorHandler: ProgramError => AppTask[Response[AppTask]] = {
    case ProgramError.UserAlreadyExists => Conflict()
    case ProgramError.UserError(_)      => InternalServerError()
    case ProgramError.ConsoleError(_)   => InternalServerError()
    case ProgramError.ClockError(_)     => InternalServerError()
  }
}

object UsersRoutes {
  def apply(envs: Environments, usersRef: Ref[Map[User.Id, User]]): UsersRoutes =
    new UsersRoutes(envs, usersRef)
  type AppTask[A] = ZIO[Any, Throwable, A]
  final case class AllUsersResponse(users: List[User])
  final case class CreateUserRequest(name: String)

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults
  implicit val userIdEncoder: Encoder[User.Id] = deriveConfiguredEncoder[User.Id]
  implicit val userEncoder: Encoder[User] = deriveConfiguredEncoder[User]
  implicit val allUsersRespEncoder: Encoder[AllUsersResponse] = deriveConfiguredEncoder[AllUsersResponse]

  implicit val createUserReqDecoder: Decoder[CreateUserRequest] = deriveConfiguredDecoder[CreateUserRequest]
}
