package io.abp.users.interfaces.http

import cats.arrow.FunctionK
import dev.profunktor.tracer.{Http4sTracerDsl, TracedHttpRoute, Tracer}
import io.abp.users.domain.User
import io.abp.users.interfaces.http.UsersRoutes._
import io.abp.users.programs.UserProgram
import io.abp.users.programs.UserProgram.ProgramError
import io.abp.users.services.users.UserService
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Response}
import zio._
import zio.interop.catz._
import zio.telemetry.opentracing.OpenTracing

class UsersRoutes[Env: Tagged](implicit tracer: Tracer[AppTask[Env, ?]]) {
  //TODO: PR in profunktor/tracer repo for scala 2.13
  val ZIOHttp4sTracerDsl = new Http4sTracerDsl[AppTask[Env, ?]] {
    override val liftG: FunctionK[AppTask[Env, ?], AppTask[Env, ?]] = FunctionK.id[AppTask[Env, ?]]
  }
  val dsl = ZIOHttp4sTracerDsl
  import dsl._

  private val PathPrefix = "/users"

  val routes = TracedHttpRoute[AppTask[Env, ?]] {

    case GET -> Root using traceId =>
      UserProgram.getAllUsers
        .foldM(errorHandler, users => Ok(AllUsersResponse(users)))
        .root(s"$traceId UserRoutes - Get All Users")

    case GET -> Root / id using traceId =>
      UserProgram
        .getUser(User.Id(id))
        .foldM(errorHandler, user => Ok(GetUserResponse(user)))
        .root(s"$traceId UserRoutes - Get User")

    case tr @ POST -> Root using traceId =>
      tr.request.as[CreateUserRequest].flatMap { req =>
        UserProgram
          .createUser(req.name)
          .foldM(
            errorHandler,
            id => Ok(CreateUserResponse(id))
          )
          .root(s"$traceId UserRoutes - Create User")
      }
  }

  //TODO: improve error handling
  private def errorHandler: ProgramError => AppTask[Env, Response[AppTask[Env, ?]]] = {
    case ProgramError.UserAlreadyExists => Conflict("User already exists")
    case ProgramError.UserError(_)      => InternalServerError()
    case ProgramError.ConsoleError(_)   => InternalServerError()
    case ProgramError.ClockError(_)     => InternalServerError()
  }

  val prefixedRoutes: HttpRoutes[AppTask[Env, ?]] = Router(
    PathPrefix -> routes
  )
}

object UsersRoutes {
  def apply[Env: Tagged](implicit tracer: Tracer[AppTask[Env, ?]]): UsersRoutes[Env] =
    new UsersRoutes[Env]

  type AppTask[Env, A] = RIO[Env with UserService[Env] with OpenTracing, A]

  final case class AllUsersResponse(users: List[User])
  final case class GetUserResponse(user: Option[User])
  final case class CreateUserRequest(name: String)
  final case class CreateUserResponse(id: User.Id)

  implicit val userIdEncoder: Encoder[User.Id] = deriveEncoder[User.Id]
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val allUsersRespEncoder: Encoder[AllUsersResponse] = deriveEncoder[AllUsersResponse]
  implicit val createUserRespEncoder: Encoder[CreateUserResponse] =
    deriveEncoder[CreateUserResponse]
  implicit val getUserRespEncoder: Encoder[GetUserResponse] = deriveEncoder[GetUserResponse]

  implicit val createUserReqDecoder: Decoder[CreateUserRequest] = deriveDecoder[CreateUserRequest]

}
