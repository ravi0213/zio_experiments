package io.abp.users.services.users.interpreters

import io.abp.users.domain.User
import io.abp.users.services.users.UserService
import io.abp.users.services.users.UserService.Error._
import zio._
import zio.telemetry.opentracing._
import zio.telemetry.opentracing.OpenTracing

object Tracing {
  def interpreter(underlying: UserService[ZIO]) =
    new UserService[ZIO] {
      type Env = underlying.Env with OpenTracing

      final def all: ZIO[Env, GetError, List[User]] =
        underlying.all
          .span("UserService - Get All Users")

      final def get(
          id: User.Id
      ): ZIO[Env, GetError, Option[User]] =
        underlying
          .get(id)
          .span("UserService - Get User")

      final def getByName(name: String): ZIO[Env, GetByNameError, List[User]] =
        underlying
          .getByName(name)
          .span("UserService - Get User By Name")

      final def create(
          name: String
      ): ZIO[Env, CreateError, User] =
        underlying
          .create(name)
          .span("UserService - Create User")
    }
}
