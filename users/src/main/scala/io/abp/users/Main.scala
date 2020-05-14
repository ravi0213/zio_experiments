package io.abp.users

import io.abp.users.config.AppConfig
import io.abp.users.domain.User
import io.abp.users.effects.idGenerator.IdGenerator
import io.abp.users.modules.Environments
import io.abp.users.modules.Logger
import io.abp.users.modules.Server
import io.abp.users.modules.Services
import zio._
import zio.clock.Clock
import zio.interop.catz._
import zio.logging._
import zio.telemetry.opentracing.OpenTracing

object Main extends App {
  type Env = Clock with IdGenerator with Logging with OpenTracing

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    (for {
      config <- loadConfig
      envs = new Environments(config)
      _ <- runApplication(config, envs)
        .provideCustomLayer(envs.userProgramEnv)
    } yield ())
      .provideCustomLayer(Logger.slf4jLogger)
      .fold(_ => 1, _ => 0)
  }

  private def loadConfig: RIO[ZEnv with Logging, AppConfig] =
    AppConfig.loadFromEnv
      .attempt[Task]
      .flatMap {
        case Right(config) =>
          log.info(s"Successfully loaded config: $config") *> ZIO.succeed(config)
        case Left(error) =>
          log.error(s"Errors while loading config: ${error.messages}", Cause.fail(error)) *> ZIO
            .fail(error.throwable)
      }

  private def runApplication(
      config: AppConfig,
      envs: Environments
  ): RIO[ZEnv with Logging, Unit] = {
    log.info("Application resources loaded, running Application") *>
      (for {
        ref <- Ref.make(Map.empty[User.Id, User])
        _ <-
          Server
            .serve[Env](config.api)
            .provideCustomLayer(envs.userProgramEnv ++ ZLayer.succeed(Services.userService(ref)))
      } yield ())
        .onError {
          case e =>
            log.error("Couldn't start the application", e)
        } *> log.info("Server running")
  }

}
