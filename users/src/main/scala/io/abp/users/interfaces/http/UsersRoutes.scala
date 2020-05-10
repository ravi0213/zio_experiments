package io.abp.users.interfaces.http

import io.abp.users.domain.User
import io.abp.users.interfaces.http.UsersRoutes._
import io.abp.users.programs.UserProgram
import io.abp.users.programs.UserProgram.ProgramError
import io.abp.users.services.users.UserService
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import zio._
import zio.interop.catz._

class UsersRoutes[Env: Tagged] {
  type AppTask[A] = ZIO[Env with UserService[Env], Throwable, A]
  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  private val pathPrefix = Root / "users"
  val routes = HttpRoutes.of[AppTask] {
    case GET -> `pathPrefix` =>
      UserProgram.getAllUsers
        .foldM(errorHandler, users => Ok(AllUsersResponse(users)))
    case GET -> `pathPrefix` / id =>
      UserProgram
        .getUser(User.Id(id))
        .foldM(errorHandler, user => Ok(GetUserResponse(user)))

    case request @ POST -> `pathPrefix` =>
      request.as[CreateUserRequest].flatMap { req =>
        UserProgram
          .createUser(req.name)
          .foldM(errorHandler, id => Ok(CreateUserResponse(id)))
      }
  }

  //TODO: improve error handling
  private def errorHandler: ProgramError => AppTask[Response[AppTask]] = {
    case ProgramError.UserAlreadyExists => Conflict("User already exists")
    case ProgramError.UserError(_)      => InternalServerError()
    case ProgramError.ConsoleError(_)   => InternalServerError()
    case ProgramError.ClockError(_)     => InternalServerError()
  }
}

object UsersRoutes {
  def apply[Env: Tagged]: UsersRoutes[Env] = new UsersRoutes[Env]()

  final case class AllUsersResponse(users: List[User])
  final case class GetUserResponse(user: Option[User])
  final case class CreateUserRequest(name: String)
  final case class CreateUserResponse(id: User.Id)

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults
  implicit val userIdEncoder: Encoder[User.Id] = deriveConfiguredEncoder[User.Id]
  implicit val userEncoder: Encoder[User] = deriveConfiguredEncoder[User]
  implicit val allUsersRespEncoder: Encoder[AllUsersResponse] = deriveConfiguredEncoder[AllUsersResponse]
  implicit val createUserRespEncoder: Encoder[CreateUserResponse] =
    deriveConfiguredEncoder[CreateUserResponse]
  implicit val getUserRespEncoder: Encoder[GetUserResponse] = deriveConfiguredEncoder[GetUserResponse]

  implicit val createUserReqDecoder: Decoder[CreateUserRequest] = deriveConfiguredDecoder[CreateUserRequest]
}
