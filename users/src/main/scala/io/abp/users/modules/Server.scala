package io.abp.users.modules

import cats.arrow.FunctionK
import cats.syntax.semigroupk._
import dev.profunktor.tracer.instances.tracerlog
import dev.profunktor.tracer.Tracer
import io.abp.users.config.ApiConfig
import io.abp.users.interfaces.http.{SystemRoutes, UsersRoutes}
import io.abp.users.modules.Timers
import io.abp.users.services.users.UserService
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{AutoSlash, CORS, Logger => RequestResponseLogger}
import org.http4s.server.Router
import zio._
import zio.interop.catz._
import zio.telemetry.opentracing.OpenTracing

object Server {
  type AppTaskEnv[Env] = Env with UserService[Env] with OpenTracing

  def serve[Env: Tagged](
      apiConfig: ApiConfig
  ): RIO[ZEnv with AppTaskEnv[Env], Unit] = {
    type AppTask[A] = RIO[AppTaskEnv[Env], A]

    implicit val tracerLog = tracerlog.defaultLog[AppTask]
    implicit val tracer = Tracer.create[AppTask]()
    val timer = new Timers[AppTaskEnv[Env]]
    import timer._

    val middleware = { routes: HttpRoutes[AppTask] =>
      AutoSlash(routes)
    } andThen { routes: HttpRoutes[AppTask] =>
      CORS(routes, CORS.DefaultCORSConfig)
    //TODO: Figure out how to add back the Timeout logging
    } andThen { routes: HttpRoutes[AppTask] =>
      RequestResponseLogger(apiConfig.logHeaders, apiConfig.logBody, FunctionK.id[AppTask])(routes.orNotFound)
    } andThen { routes: Http[AppTask, AppTask] =>
      Tracer[AppTask].middleware(routes: HttpApp[AppTask], false, false)
    }

    for {
      v1Routes <- ZIO.succeed(
        SystemRoutes[AppTask]().routes <+> UsersRoutes[Env].prefixedRoutes
      )
      //v2Routes <- ZIO.succeed(
      //  v2.SystemRoutes[AppTask]().routes <+> v2.UsersRoutes[Env].prefixedRoutes
      //)
      router <- ZIO.succeed(
        Router(
          "/" -> v1Routes, //This would always point to the oldest supported version
          "/v1" -> v1Routes
          //"/v2" -> v2Routes
        )
      )
      implicit0(rts: Runtime[ZEnv with AppTaskEnv[Env]]) <- ZIO.runtime[ZEnv with AppTaskEnv[Env]]
      _ <-
        BlazeServerBuilder[AppTask](rts.platform.executor.asEC)
          .bindHttp(apiConfig.port, apiConfig.host)
          .withHttpApp(middleware(router))
          .serve
          .compile
          .drain
    } yield ()
  }
}
