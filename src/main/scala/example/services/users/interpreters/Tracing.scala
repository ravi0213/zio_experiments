package example.services.users.interpreters

import java.time.OffsetDateTime

import example.domain.User
import example.services.users.UserService
import example.services.users.UserService.Error._
import zio._
import zio.telemetry.opentracing._
import zio.telemetry.opentracing.OpenTracing

object Tracing {
  def interpreter(underlying: UserService[ZIO]) =
    new UserService[ZIO] {
      type Env = underlying.Env with OpenTracing

      final def get(
          id: Long
      ): ZIO[Env, GetError, Option[User]] =
        underlying
          .get(id)
          .span("UserService - Get User")

      final def create(
          id: Long,
          name: String,
          createdAt: OffsetDateTime
      ): ZIO[Env, CreateError, Unit] =
        underlying
          .create(id, name, createdAt)
          .span("UserService - Create User")
    }
}
