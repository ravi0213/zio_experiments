package io.abp.users.config

import scala.concurrent.duration._

import io.abp.users.config.LoggingConfig.LoggerConfig
import io.abp.users.config.TelemetryConfig.TracerConfig

case class AppConfig(telemetry: TelemetryConfig, api: ApiConfig, logging: LoggingConfig)
case class ApiConfig(
    host: String,
    port: Int,
    responseTimeout: FiniteDuration,
    logHeaders: Boolean,
    logBody: Boolean
)
case class TelemetryConfig(tracerConfig: TracerConfig)
object TelemetryConfig {
  sealed trait TracerConfig
  object TracerConfig {
    case object Mock extends TracerConfig
    case class JaegerConfig(host: String, serviceName: String) extends TracerConfig
  }
}

case class LoggingConfig(loggerConfig: LoggerConfig)
object LoggingConfig {
  sealed trait LoggerConfig
  object LoggerConfig {
    case object Mock extends LoggerConfig
    case object Live extends LoggerConfig
  }
}

object AppConfig {
  def live =
    AppConfig(
      TelemetryConfig(TracerConfig.JaegerConfig("0.0.0.0:9411", "zio-experiments")),
      api = ApiConfig(
        host = "localhost",
        port = 8080,
        responseTimeout = 10.seconds,
        logHeaders = true,
        logBody = true
      ),
      logging = LoggingConfig(LoggerConfig.Live)
    )

  def mock =
    AppConfig(
      TelemetryConfig(TracerConfig.Mock),
      api = ApiConfig(
        host = "localhost",
        port = 8080,
        responseTimeout = 10.seconds,
        logHeaders = true,
        logBody = true
      ),
      logging = LoggingConfig(LoggerConfig.Mock)
    )
}
