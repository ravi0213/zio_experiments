package example.modules

import example.config.{AppConfig, TelemetryConfig}
import example.effects.log._
import example.telemetry.Tracer
import zio.clock._
import zio.console._
import zio.random._
import zio.telemetry.opentracing.OpenTracing

class Programs(config: AppConfig) {
  private val tracer = config.telemetry.tracerConfig match {
    case TelemetryConfig.TracerConfig.Mock => Tracer.mock
    case TelemetryConfig.TracerConfig.JaegerConfig(host, serviceName) =>
      Tracer.jaeger(host, serviceName)
  }
  private val defaultEnv = Clock.live ++ Console.live ++ Random.live
  val userProgramEnv =
    defaultEnv ++ Logging.consoleLogger ++ (Clock.live >>> OpenTracing.live(tracer))

}
