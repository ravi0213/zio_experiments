package io.abp.users.config

import scala.concurrent.duration._

import io.abp.users.config.TelemetryConfig.TracerConfig

case class AppConfig(telemetry: TelemetryConfig, api: ApiConfig)
case class ApiConfig(
    host: String,
    port: Int,
    responseTimeout: FiniteDuration,
    logHeaders: Boolean,
    logBody: Boolean
)
case class TelemetryConfig(enabled: Boolean, tracerConfig: TracerConfig)
object TelemetryConfig {
  sealed trait TracerConfig
  object TracerConfig {
    case object Mock extends TracerConfig
    case class JaegerConfig(host: String, serviceName: String) extends TracerConfig
  }
}

object AppConfig {
  def live =
    AppConfig(
      TelemetryConfig(enabled = true, TracerConfig.JaegerConfig("0.0.0.0:9411", "zio-experiments")),
      api = ApiConfig(
        host = "localhost",
        port = 8080,
        responseTimeout = 10.seconds,
        logHeaders = true,
        logBody = true
      )
    )

  def mock =
    AppConfig(
      TelemetryConfig(enabled = true, TracerConfig.Mock),
      api = ApiConfig(
        host = "localhost",
        port = 8080,
        responseTimeout = 10.seconds,
        logHeaders = true,
        logBody = true
      )
    )
}
