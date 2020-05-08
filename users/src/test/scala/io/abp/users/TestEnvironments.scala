package io.abp.users

import io.abp.users.config.AppConfig
import io.abp.users.effects.idGenerator._
import io.abp.users.effects.log._
import io.abp.users.fixtures._
import io.abp.users.mocks._
import io.abp.users.modules.Environments
import io.abp.users.services.users._
import zio.clock._
import zio.console._
import zio.random._
import zio.telemetry.opentracing.OpenTracing
import zio.ZLayer

class TestEnvironments(
    val env: ZLayer[Any, Nothing, TestEnvironments.Env]
) extends Environments(AppConfig.mock) {

  override val userProgramEnv = env
}
object TestEnvironments {

  def apply(
      testIdGenerator: ZLayer[Any, Nothing, IdGenerator] = testIdGenerator,
      testClock: ZLayer[Any, Nothing, Clock] = testClock,
      testOpenTracing: ZLayer[Clock, Nothing, OpenTracing] = testOpenTracing,
      testLogging: ZLayer[Any, Nothing, Logging] = testLogging,
      testUserService: ZLayer[Any, Nothing, UserService] = testUserService,
      testConsole: ZLayer[Any, Nothing, Console] = testConsole,
      testRandom: ZLayer[Any, Nothing, Random] = testRandom
  ): TestEnvironments =
    new TestEnvironments(
      testClock ++ testConsole ++ testRandom ++ testLogging ++ (testClock >>> testOpenTracing) ++ testIdGenerator ++ testUserService
    )

  type Env = Clock with Console with Random with Logging with OpenTracing with IdGenerator with UserService
  val testIdGenerator = testIdGeneratorMock(fixedUserId)
  val testClock = testClockMock(fixedDateTime)
  val testOpenTracing = OpenTracing.noop
  val testLogging = Logging.consoleLogger
  val testUserService = User.inMemory()
  val testConsole = Console.live
  val testRandom = Random.live
}
