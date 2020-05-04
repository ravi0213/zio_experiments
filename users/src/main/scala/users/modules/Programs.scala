package users.modules

import users.config.AppConfig
import users.config.TelemetryConfig.TracerConfig
import users.effects.idGenerator._
import users.effects.log._
import users.telemetry.Tracer
import zio.clock._
import zio.console._
import zio.random._
import zio.telemetry.opentracing.OpenTracing

class Programs(config: AppConfig) {
  private val tracer = config.telemetry.tracerConfig match {
    case TracerConfig.Mock                            => Tracer.mock
    case TracerConfig.JaegerConfig(host, serviceName) => Tracer.jaeger(host, serviceName)
  }
  private val defaultEnv = Clock.live ++ Console.live ++ Random.live
  val userProgramEnv =
    defaultEnv ++ Logging.consoleLogger ++ (Clock.live >>> OpenTracing.live(tracer)) ++ IdGenerator.live

}
