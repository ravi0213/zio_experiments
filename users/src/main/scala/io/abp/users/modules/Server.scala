package io.abp.users.modules

import cats.arrow.FunctionK
import io.abp.users.config.ApiConfig
import io.abp.users.interfaces.http.SystemRoutes
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{AutoSlash, CORS, Logger => RequestResponseLogger}
import org.http4s.{HttpApp, HttpRoutes}
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object Server {
  def serve(apiConfig: ApiConfig): ZIO[ZEnv, Throwable, Unit] = {
    val middleware: HttpRoutes[Task] => HttpApp[Task] = { routes: HttpRoutes[Task] =>
      AutoSlash(routes)
    } andThen { routes: HttpRoutes[Task] =>
      CORS(routes, CORS.DefaultCORSConfig)
    } andThen { routes: HttpRoutes[Task] =>
      RequestResponseLogger(apiConfig.logHeaders, apiConfig.logBody, FunctionK.id[Task])(routes.orNotFound)
    }

    val routes: HttpRoutes[Task] = SystemRoutes[Task]().routes

    ZIO.runtime[ZEnv].flatMap { implicit rts =>
      BlazeServerBuilder[Task](rts.platform.executor.asEC)
        .bindHttp(apiConfig.port, apiConfig.host)
        .withHttpApp(middleware(routes))
        .serve
        .compile
        .drain
    }
  }
}
