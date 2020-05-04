package io.abp.users.config

import io.abp.users.config.TelemetryConfig.TracerConfig

case class AppConfig(telemetry: TelemetryConfig)
case class TelemetryConfig(enabled: Boolean, tracerConfig: TracerConfig)
object TelemetryConfig {
  sealed trait TracerConfig
  object TracerConfig {
    case object Mock extends TracerConfig
    case class JaegerConfig(host: String, serviceName: String) extends TracerConfig
  }
}

object AppConfig {
  def live = AppConfig(
    TelemetryConfig(enabled = true, TracerConfig.JaegerConfig("0.0.0.0:9411", "zio-experiments"))
  )

  def mock = AppConfig(TelemetryConfig(enabled = true, TracerConfig.Mock))
}
