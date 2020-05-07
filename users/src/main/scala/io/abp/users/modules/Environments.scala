package io.abp.users.modules

import io.abp.users.config.AppConfig
import io.abp.users.config.TelemetryConfig.TracerConfig
import io.abp.users.effects.idGenerator._
import io.abp.users.effects.log._
import io.abp.users.telemetry.Tracer
import zio.clock._
import zio.console._
import zio.random._
import zio.telemetry.opentracing.OpenTracing

class Environments(config: AppConfig) {
  private val tracer = config.telemetry.tracerConfig match {
    case TracerConfig.Mock                            => Tracer.mock
    case TracerConfig.JaegerConfig(host, serviceName) => Tracer.jaeger(host, serviceName)
  }
  private val defaultEnv = Clock.live ++ Console.live ++ Random.live
  val userProgramEnv =
    defaultEnv ++ Logging.consoleLogger ++ (Clock.live >>> OpenTracing.live(tracer)) ++ IdGenerator.live

}
