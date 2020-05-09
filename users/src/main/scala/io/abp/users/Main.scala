package io.abp.users

import io.abp.users.config.AppConfig
import io.abp.users.domain.User
import io.abp.users.effects.log._
import io.abp.users.modules.Environments
import io.abp.users.modules.Server
import io.abp.users.modules.Services
import zio._

object Main extends App {

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
        userService = Services.userService(ref)
        _ <-
          Server
            .serve(config.api, userService)
            .provideCustomLayer(envs.userProgramEnv)
      } yield ())
        .onError {
          case e =>
            error(e)("Couldn't start the application")
        } *> info("Server running")
  }

}
