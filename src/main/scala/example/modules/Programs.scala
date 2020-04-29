package example.modules

import example.config.AppConfig
import example.config.TelemetryConfig.TracerConfig
import example.effects.idGenerator._
import example.effects.log._
import example.telemetry.Tracer
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
