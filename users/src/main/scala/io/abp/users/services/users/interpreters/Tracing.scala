package io.abp.users.services.users.interpreters

import io.abp.users.domain.User
import io.abp.users.services.users._
import io.abp.users.services.users.User.Error._
import io.abp.users.services.users.User.Service
import zio._
import zio.telemetry.opentracing._

object Tracing {
  val interpreter =
    new Service {
      final def all: ZIO[Env with UserService, GetError, List[User]] =
        allUsers
          .span("UserService - Get All Users")

      final def get(
          id: User.Id
      ): ZIO[Env with UserService, GetError, Option[User]] =
        getUser(id)
          .span("UserService - Get User")

      final def getByName(name: String): ZIO[Env with UserService, GetByNameError, List[User]] =
        getUsersByName(name)
          .span("UserService - Get User By Name")

      final def create(
          name: String
      ): ZIO[Env with UserService, CreateError, User] =
        createUser(name)
          .span("UserService - Create User")
    }
}
