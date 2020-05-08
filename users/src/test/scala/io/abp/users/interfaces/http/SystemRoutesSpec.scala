package io.abp.users.interfaces.http

import org.http4s._
import org.http4s.implicits._
import zio.interop.catz._
import zio.test._
import zio.test.Assertion._
import zio.ZIO

object SystemRoutesSpec extends DefaultRunnableSpec {
  type AppTask[A] = ZIO[Any, Throwable, A]

  override def spec =
    suite("SystemRoutes")(
      suite("Healthz route")(
        testM("should return 200") {
          val expected = Status.Ok
          retHealthz.map { result =>
            assert(result.status)(equalTo(expected))
          }
        }
      ),
      suite("Version route")(
        testM("should return 200") {
          val expected = Status.Ok
          retVersion.map { result =>
            assert(result.status)(equalTo(expected))
          }
        }
      )
    )

  private[this] val retHealthz: AppTask[Response[AppTask]] = {
    val getHealthz = Request[AppTask](Method.GET, uri"/healthz")
    SystemRoutes[AppTask]().routes.orNotFound(getHealthz)
  }

  private[this] val retVersion: AppTask[Response[AppTask]] = {
    val getVersion = Request[AppTask](Method.GET, uri"/version")
    SystemRoutes[AppTask]().routes.orNotFound(getVersion)
  }
}
