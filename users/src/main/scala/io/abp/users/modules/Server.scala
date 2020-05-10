package io.abp.users.modules

import cats.arrow.FunctionK
import cats.syntax.semigroupk._
import io.abp.users.config.ApiConfig
import io.abp.users.interfaces.http.{SystemRoutes, UsersRoutes}
import io.abp.users.modules.Timers
import io.abp.users.services.users.UserService
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{AutoSlash, CORS, Logger => RequestResponseLogger}
import org.http4s.{HttpApp, HttpRoutes}
import zio._
import zio.interop.catz._

object Server {
  def serve[Env: Tagged](
      apiConfig: ApiConfig
  ): ZIO[ZEnv with Env with UserService[Env], Throwable, Unit] = {
    type AppTask[A] = ZIO[Env with UserService[Env], Throwable, A]

    val timer = new Timers[Env with UserService[Env]]
    import timer._

    val middleware: HttpRoutes[AppTask] => HttpApp[AppTask] = { routes: HttpRoutes[AppTask] =>
      AutoSlash(routes)
    } andThen { routes: HttpRoutes[AppTask] =>
      CORS(routes, CORS.DefaultCORSConfig)
    } andThen { routes: HttpRoutes[AppTask] =>
      RequestResponseLogger(apiConfig.logHeaders, apiConfig.logBody, FunctionK.id[AppTask])(routes.orNotFound)
    }

    for {
      routes <- ZIO.succeed(
        SystemRoutes[AppTask]().routes <+> UsersRoutes[Env].routes
      )
      implicit0(rts: Runtime[ZEnv with Env with UserService[Env]]) <-
        ZIO.runtime[ZEnv with Env with UserService[Env]]
      _ <-
        BlazeServerBuilder[AppTask](rts.platform.executor.asEC)
          .bindHttp(apiConfig.port, apiConfig.host)
          .withHttpApp(middleware(routes))
          .serve
          .compile
          .drain
    } yield ()
  }
}
