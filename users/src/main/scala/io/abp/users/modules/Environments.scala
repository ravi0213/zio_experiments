package io.abp.users.modules

import io.abp.users.config._
import io.abp.users.effects.idGenerator._
import io.abp.users.telemetry.Tracer
import zio._
import zio.clock._
import zio.config._
import zio.config.Config
import zio.config.magnolia.DeriveConfigDescriptor.descriptor
import zio.console._
import zio.random._

class Environments(config: AppConfig) {

  //private val apiConfigDescriptor: ConfigDescriptor[ApiConfig] =
  //  (string("API_HOST") |@| int("API_PORT") |@| boolean("API_LOG_HEADERS") |@| boolean("API_LOG_BODY"))(
  //    ApiConfig.apply,
  //    ApiConfig.unapply
  //  )
  //val apiConfig = Config
  //  .fromSystemEnv(apiConfigDescriptor)

  //apiConfig
  //  .mapError(x => { println("ERROR"); x.prettyPrint() })
  //  .map(config => { println("OK"); println(config.toString) })
  //println("APICONFIG")

  val configDescriptor: ConfigDescriptor[AppConfig] = descriptor[AppConfig]

  //type C[A, B] = ZLayer[Cause[ReadError[String]], A, B]
  //type S[A, B] = ZLayer[Config[AppConfig], A, B]
  val configLayer: Layer[ReadError[String], Config[AppConfig]] = Config.fromSystemEnv(configDescriptor)

  configLayer
    .mapError(_.prettyPrint())
    .map(config => println(config.toString))
  println("App CONFIG")

  val logger = Logger.slf4jLogger
  val tracer = Tracer(config.telemetry)
  val clock = Clock.live
  val idGenerator = IdGenerator.live

  private val defaultEnv = configLayer ++ Clock.live ++ Console.live ++ Random.live
  val userProgramEnv = defaultEnv ++ logger ++ tracer ++ IdGenerator.live ++ configLayer

}
