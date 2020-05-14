package io.abp.users.modules

import io.abp.users.config.AppConfig
import io.abp.users.effects.idGenerator._
import io.abp.users.telemetry.Tracer
import zio.clock._
import zio.console._
import zio.random._

class Environments(config: AppConfig) {
  private val logger = Logger.slf4jLogger
  private val tracer = Tracer(config.telemetry)
  private val defaultEnv = Clock.live ++ Console.live ++ Random.live
  val userProgramEnv = defaultEnv ++ logger ++ tracer ++ IdGenerator.live

}
