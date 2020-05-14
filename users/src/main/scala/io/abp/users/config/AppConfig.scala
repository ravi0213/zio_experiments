package io.abp.users.config

import scala.concurrent.duration._

import cats.syntax.parallel._
import ciris.{env, ConfigValue}
import io.abp.users.config.TelemetryConfig.TracerConfig

case class AppConfig(telemetry: TelemetryConfig, api: ApiConfig)
case class ApiConfig(
    host: String,
    port: Int,
    logHeaders: Boolean,
    logBody: Boolean,
    responseTimeout: FiniteDuration
)
object ApiConfig {
  def loadFromEnv: ConfigValue[ApiConfig] =
    (
      env("API_HOST"),
      env("API_PORT").as[Int],
      env("API_LOG_HEADERS").as[Boolean],
      env("API_LOG_BODY").as[Boolean],
      env("API_RESPONSE_TIMEOUT").as[FiniteDuration]
    ).parMapN(ApiConfig.apply)
}
case class TelemetryConfig(tracerConfig: TracerConfig)
object TelemetryConfig {
  def loadFromEnv: ConfigValue[TelemetryConfig] =
    env("TELEMETRY_ENABLED").as[Boolean].flatMap { enabled =>
      val tracerConfig =
        if (enabled)
          (
            env("TELEMETRY_HOST"),
            env("TELEMETRY_SERVICE_NAME")
          ).parMapN(TelemetryConfig.TracerConfig.JaegerConfig)
        else
          ConfigValue.default(TelemetryConfig.TracerConfig.Mock)
      tracerConfig.map(TelemetryConfig(_))
    }

  sealed trait TracerConfig
  object TracerConfig {
    case object Mock extends TracerConfig
    case class JaegerConfig(host: String, serviceName: String) extends TracerConfig
  }
}

object AppConfig {
  def loadFromEnv: ConfigValue[AppConfig] =
    (
      TelemetryConfig.loadFromEnv,
      ApiConfig.loadFromEnv
    ).parMapN(AppConfig.apply)
}
