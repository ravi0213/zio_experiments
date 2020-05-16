package io.abp.users.modules

import io.abp.users.config.AppConfig
import io.abp.users.effects.idGenerator._
import io.abp.users.telemetry.Tracer
import zio.clock._
import zio.config._
import zio.config.Config
import zio.config.magnolia.DeriveConfigDescriptor.descriptor
import zio.console._
import zio.random._

class Environments(config: AppConfig) {

  val configDescriptor: ConfigDescriptor[AppConfig] = descriptor[AppConfig]
  val configLayer = Config.fromSystemEnv(configDescriptor)
  println(configLayer)

  private val logger = Logger.slf4jLogger
  private val tracer = Tracer(config.telemetry)
  private val defaultEnv = Clock.live ++ Console.live ++ Random.live
  val userProgramEnv = defaultEnv ++ logger ++ tracer ++ IdGenerator.live ++ configLayer

}
