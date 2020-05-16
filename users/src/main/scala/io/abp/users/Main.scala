package io.abp.users

import io.abp.users.config._
import io.abp.users.domain.User
import io.abp.users.effects.idGenerator.IdGenerator
import io.abp.users.modules.Environments
import io.abp.users.modules.Logger
import io.abp.users.modules.Server
import io.abp.users.modules.Services
import zio._
import zio.clock.Clock
import zio.config.{config => zioconfig, _}
import zio.interop.catz._
import zio.logging._
import zio.telemetry.opentracing.OpenTracing

object Main extends App {
  type Env =
    Config[AppConfig] with Clock with IdGenerator with Logging with OpenTracing

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    (for {
      config <- loadConfig
      _ = println("BEFORE LOAD ENV")
      envs = new Environments(config)
      _ = println("BEFORE READ ENV")
      appConfig <- zioconfig[ApiConfig]
      _ = println("LOAD ENV")
      _ = println(appConfig)
      _ = println("AFTER READ ENV")
      _ <- runApplication(config, envs)
        .provideCustomLayer(envs.userProgramEnv ++ envs.configLayer)
    } yield ())
      .onError { e =>
        println("WTF")
        log.error(s"Couldn't start the application. Error: ${e.failures.map(_.getMessage).mkString}", e)
      }
      .provideCustomLayer(Logger.slf4jLogger ++ AppConfig.myConfig)
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
  ): ZIO[ZEnv with Logging, Throwable, Unit] = {
    log.info("Application resources loaded, running Application") *>
      (for {
        ref <- Ref.make(Map.empty[User.Id, User])
        _ <-
          Server
            .serve[Env](config.api)
            .provideCustomLayer(envs.userProgramEnv ++ Services.userService(envs, ref))
      } yield ())
        .onError { e =>
          println("WTF")
          log.error("Couldn't start the application", e)
        } //*> log.info("Server running")
        .onExit(_ => { println("FUUUUCK"); log.error("FUUUCK") })
  }

}
