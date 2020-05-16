package io.abp.users

import scala.concurrent.duration._

import io.abp.users.config._
import io.abp.users.effects.idGenerator._
import io.abp.users.fixtures._
import io.abp.users.mocks._
import io.abp.users.modules.Environments
import zio.clock._
import zio.console._
import zio.logging._
import zio.random._
import zio.telemetry.opentracing.OpenTracing
import zio.ZLayer

class TestEnvironments(
    val env: ZLayer[Any, Nothing, TestEnvironments.Env]
) extends Environments(TestEnvironments.config) {

  override val userProgramEnv = env
}
object TestEnvironments {

  val config =
    AppConfig(
      Environment.Local,
      TelemetryConfig(TelemetryConfig.TracerConfig.Mock),
      api = ApiConfig(
        host = "localhost",
        port = 8080,
        responseTimeout = 10.seconds,
        logHeaders = true,
        logBody = true
      )
    )

  def apply(
      testIdGenerator: ZLayer[Any, Nothing, IdGenerator] = testIdGenerator,
      testClock: ZLayer[Any, Nothing, Clock] = testClock,
      testOpenTracing: ZLayer[Clock, Nothing, OpenTracing] = testOpenTracing,
      testLogging: ZLayer[Any, Nothing, Logging] = testLogging,
      testConsole: ZLayer[Any, Nothing, Console] = testConsole,
      testRandom: ZLayer[Any, Nothing, Random] = testRandom
  ): TestEnvironments =
    new TestEnvironments(
      testClock ++ testConsole ++ testRandom ++ testLogging ++ (testClock >>> testOpenTracing) ++ testIdGenerator
    )

  type Env = Clock with Console with Random with Logging with OpenTracing with IdGenerator
  val testIdGenerator = testIdGeneratorMock(fixedUserId)
  val testClock = testClockMock(fixedDateTime)
  val testOpenTracing = OpenTracing.noop

  private val logFormat = "%s"
  private def logFormatWithContext(context: LogContext, message: => String) = {
    logFormat.format(message, context)
  }
  val testLogging = (Clock.live ++ Console.live) >>> Logging.console(logFormatWithContext)
  val testConsole = Console.live
  val testRandom = Random.live
}
