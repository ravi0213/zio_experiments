package io.abp.users.interfaces.http

import cats.{Applicative, Defer}
import io.abp.users.BuildInfo
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import SystemRoutes._

class SystemRoutes[F[_]: Defer: Applicative]() extends Http4sDsl[F] {
  val routes = HttpRoutes.of[F] {
    case GET -> Root / "healthz" => Ok("ok")
    case GET -> Root / "version" => Ok(VersionResp.current)
  }
}

object SystemRoutes {
  def apply[F[_]: Defer: Applicative](): SystemRoutes[F] = new SystemRoutes[F]()

  final case class VersionResp private (
      name: String,
      version: String,
      scalaVersion: String,
      sbtVersion: String,
      gitHeadCommit: Option[String]
  )
  object VersionResp {
    val current: VersionResp =
      VersionResp(
        BuildInfo.name,
        BuildInfo.version,
        BuildInfo.scalaVersion,
        BuildInfo.sbtVersion,
        BuildInfo.gitHeadCommit
      )
  }

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults
  implicit val versionRespEncoder: Encoder[VersionResp] = deriveConfiguredEncoder[VersionResp]

  implicit def entityEncoder[F[_], A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
}
