package io.abp.users

import io.abp.users.config.AppConfig
import io.abp.users.domain.User
import io.abp.users.effects.idGenerator.IdGenerator
import io.abp.users.effects.log._
import io.abp.users.effects.log.Logging
import io.abp.users.modules.Environments
import io.abp.users.modules.Server
import io.abp.users.modules.Services
import zio._
import zio.clock.Clock
import zio.telemetry.opentracing.OpenTracing

object Main extends App {
  type ServiceEnv = Clock with IdGenerator with Logging with OpenTracing

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val config = AppConfig.live
    val envs = new Environments(config)

    runApplication(config, envs)
      .provideCustomLayer(envs.userProgramEnv)
      .fold(_ => 1, _ => 0)
  }

  private def runApplication(
      config: AppConfig,
      envs: Environments
  ): ZIO[ZEnv with Logging, Throwable, Unit] = {
    info("Application resources loaded, running Application") *>
      (for {
        ref <- Ref.make(Map.empty[User.Id, User])
        _ <-
          Server
            .serve[ServiceEnv](config.api)
            .provideCustomLayer(envs.userProgramEnv ++ ZLayer.succeed(Services.userService(ref)))
      } yield ())
        .onError {
          case e =>
            error(e)("Couldn't start the application")
        } *> info("Server running")
  }

}
