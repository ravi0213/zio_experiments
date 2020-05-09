package io.abp.users.services.users.interpreters

import io.abp.users.domain.User
import io.abp.users.services.users.User.Error._
import io.abp.users.services.users.User.Service
import zio._
import zio.telemetry.opentracing._

object Tracing {
  def interpreter[Env](underlying: Service[Env]): Service[Env with OpenTracing] = {
    type WithTracing = Env with OpenTracing
    new Service[WithTracing] {
      final def all: ZIO[WithTracing, GetError, List[User]] =
        underlying.all
          .span("UserService - Get All Users")

      final def get(
          id: User.Id
      ): ZIO[WithTracing, GetError, Option[User]] =
        underlying
          .get(id)
          .span("UserService - Get User")

      final def getByName(name: String): ZIO[WithTracing, GetByNameError, List[User]] =
        underlying
          .getByName(name)
          .span("UserService - Get User By Name")

      final def create(
          name: String
      ): ZIO[WithTracing, CreateError, User] =
        underlying
          .create(name)
          .span("UserService - Create User")
    }
  }
}
