package io.abp.users.interfaces.http

import org.http4s._
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import zio.test._
import zio.test.Assertion._

object SystemRoutesSpec extends DefaultRunnableSpec {
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

  private[this] val retHealthz: Task[Response[Task]] = {
    val getHealthz = Request[Task](Method.GET, uri"/healthz")
    SystemRoutes[Task]().routes.orNotFound(getHealthz)
  }

  private[this] val retVersion: Task[Response[Task]] = {
    val getVersion = Request[Task](Method.GET, uri"/version")
    SystemRoutes[Task]().routes.orNotFound(getVersion)
  }
}
