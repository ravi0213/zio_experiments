package io.abp.users.config

//import scala.concurrent.duration._

import cats.syntax.parallel._
import ciris.{env, ConfigValue}
import enumeratum.EnumEntry.Hyphencase
import enumeratum.{CirisEnum, Enum, EnumEntry}
import io.abp.users.config.TelemetryConfig.TracerConfig

case class AppConfig(environment: Environment, telemetry: TelemetryConfig, api: ApiConfig)

sealed abstract class Environment extends EnumEntry with Hyphencase {
  val name = this.entryName
}

object Environment extends Enum[Environment] with CirisEnum[Environment] {
  case object Local extends Environment
  case object Staging extends Environment
  case object Production extends Environment

  override val values = findValues

  def loadFromEnv: ConfigValue[Environment] = env("ENVIRONMENT").as[Environment]
}

case class ApiConfig(
    host: String,
    port: Int,
    logHeaders: Boolean,
    logBody: Boolean
    //responseTimeout: FiniteDuration
)
object ApiConfig {
  def loadFromEnv: ConfigValue[ApiConfig] =
    (
      env("API_HOST"),
      env("API_PORT").as[Int],
      env("API_LOG_HEADERS").as[Boolean],
      env("API_LOG_BODY").as[Boolean]
      // env("API_RESPONSE_TIMEOUT").as[FiniteDuration]
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
      Environment.loadFromEnv,
      TelemetryConfig.loadFromEnv,
      ApiConfig.loadFromEnv
    ).parMapN(AppConfig.apply)

//TODO: Here are the current issues
// - No way to map on a ConfigDescriptor
// - No way to lift into the ConfigDescriptor context
// - No support for FiniteDuration (maybe use ZIO.Duration instead)
// - No configurabiliy for parameter names with Magnolia (e.g. API_HOST -> ApiConfig(host: String))

  import zio._
  import zio.config._
  import cats.syntax.option._
  import zio.config.{PropertyType, ConfigDescriptor}, ConfigDescriptor._, ConfigDescriptorAdt.Source,
  PropertyType.PropertyReadError
  //import zio.config.magnolia.DeriveConfigDescriptor.descriptor
  println("START")
  private val apiConfigDescriptor: ConfigDescriptor[ApiConfig] =
    (string("API_HOST") |@| int("API_PORT") |@| boolean("API_LOG_HEADERS") |@| boolean("API_LOG_BODY"))(
      ApiConfig.apply,
      ApiConfig.unapply
    )
  private val environmentConfigDescriptor: ConfigDescriptor[Environment] =
    (string("ENVIRONMENT"))(Environment.withName, _.name.some)

  case object TracerConfigType extends PropertyType[String, TelemetryConfig.TracerConfig] {
    def read(value: String): Either[PropertyReadError[String], TelemetryConfig.TracerConfig] =
      Right(TelemetryConfig.TracerConfig.Mock)

    def write(value: TelemetryConfig.TracerConfig): String =
      value match {
        case TelemetryConfig.TracerConfig.Mock                     => "mock"
        case TelemetryConfig.TracerConfig.JaegerConfig(host, name) => s"jaegerConfig($host, $name)"
      }
  }

  private val telemetryConfigDescriptor: ConfigDescriptor[TelemetryConfig.TracerConfig] =
    boolean("TELEMETRY_ENABLED").flatMap(
      { enabled =>
        val tracerConfig =
          if (enabled)
            (string("TELEMETRY_HOST") |@| string("TELEMETRY_SERVICE_NAME"))(
              TelemetryConfig.TracerConfig.JaegerConfig.apply,
              TelemetryConfig.TracerConfig.JaegerConfig.unapply
            )
          else
            Source(ConfigSource.empty, TracerConfigType)
        tracerConfig
      }
    )

  private val configDescriptor =
    (environmentConfigDescriptor |@| telemetryConfigDescriptor |@| apiConfigDescriptor)(
      (env, tracer, api) => AppConfig(env, TelemetryConfig(tracer), api),
      (appConfig: AppConfig) =>
        AppConfig
          .unapply(appConfig)
          .map {
            case (env: Environment, telemetry: TelemetryConfig, api: ApiConfig) =>
              (env, telemetry.tracerConfig, api)
          }
    )

  val myConfig = ZLayer.fromEffect(
    ConfigSource.fromSystemEnv
      .flatMap { source =>
        ZIO.fromEither(read(configDescriptor from source))
      }
  )
  //.mapError(x => { println("ERROR"); x })
  //.map(config => { println("OK"); println(config.toString) })
  println("MYCONFIG")
}
