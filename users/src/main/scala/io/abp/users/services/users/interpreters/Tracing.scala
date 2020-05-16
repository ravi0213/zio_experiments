package io.abp.users.services.users.interpreters

import io.abp.users.config.AppConfig
import io.abp.users.domain.User
import io.abp.users.services.users.User.Error._
import io.abp.users.services.users.User.Service
import zio.config._
import zio.telemetry.opentracing._
import zio.ZIO

object Tracing {
  type WithTracing[Env] = Env with OpenTracing with Config[AppConfig]
  def interpreter[Env](underlying: Service[Env]): Service[WithTracing[Env]] = {
    new Service[WithTracing[Env]] {
      final def all: ZIO[WithTracing[Env], AllError, List[User]] =
        for {
          config <- config[AppConfig]
          result <-
            underlying.all
              .span("UserService - Get All Users")
              .tag("environment", config.environment.name)
        } yield result

      final def get(
          id: User.Id
      ): ZIO[WithTracing[Env], GetError, Option[User]] =
        underlying
          .get(id)
          .span("UserService - Get User")

      final def getByName(name: String): ZIO[WithTracing[Env], GetByNameError, List[User]] =
        underlying
          .getByName(name)
          .span("UserService - Get User By Name")

      final def create(
          name: String
      ): ZIO[WithTracing[Env], CreateError, User] =
        underlying
          .create(name)
          .span("UserService - Create User")
    }
  }
}
