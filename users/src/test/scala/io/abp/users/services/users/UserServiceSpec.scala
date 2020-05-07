package io.abp.users.services.users

import java.time.OffsetDateTime

import io.abp.users.domain.User
import io.abp.users.effects.idGenerator._
import io.abp.users.effects.log._
import io.abp.users.generators._
import io.abp.users.mocks._
import io.abp.users.services.users.{User => UserService, _}
import zio.telemetry.opentracing.OpenTracing
import zio.test._
import zio.test.Assertion._
import zio.test.environment._
import zio.test.Gen._
import zio.ZIO

object UserServiceSpec extends DefaultRunnableSpec {
  val fixedUserId = User.Id("user_dbe1bc85-7f06-404e-ac8c-ae6661ff2bb6")
  val fixedDateTime = OffsetDateTime.parse("2007-12-03T10:15:30+01:00")
  val env = testIdGenerator(fixedUserId) ++ testClock(
    fixedDateTime
  ) ++ OpenTracing.noop ++ Logging.consoleLogger

  override def spec =
    suite("UserService")(
      suite("create")(
        testM("should create a new user and return it") {
          checkM(anyString) { name =>
            val expected = User(fixedUserId, name, fixedDateTime)
            createUser(name)
              .map { result => assert(result)(equalTo(expected)) }
              .provideLayer(UserService.live() ++ env)
          }
        }
      ),
      suite("get")(
        testM("should return the user with the corresponding id") {
          checkM(Gen.listOfN(10)(userGen)) { users =>
            val name = "Alex"
            val expected = Some(User(fixedUserId, name, fixedDateTime))
            (for {
              _ <- createUser(name)
              result <- getUser(fixedUserId)
            } yield assert(result)(equalTo(expected))).provideLayer(UserService.live(users.toM) ++ env)
          }.provideLayer(testEnvironment ++ testIdGenerator(fixedUserId))
        }
      ),
      suite("getByName")(
        testM("should return the list of users with the corresponding name") {
          checkM(Gen.listOfN(10)(userGen)) { users =>
            val name = "Alex"
            val expected = List(User(fixedUserId, name, fixedDateTime))
            (for {
              _ <- createUser(name)
              result <- getUsersByName(name)
            } yield assert(result)(equalTo(expected))).provideLayer(UserService.live(users.toM) ++ env)
          }.provideLayer(testEnvironment ++ testIdGenerator(fixedUserId))
        }
      ),
      suite("all")(
        testM("should return the list of all users") {
          checkM(Gen.listOfN(10)(userGen)) { users =>
            val name = "Alex"
            (for {
              user <- createUser(name)
              expected <- ZIO.succeed(user +: users)
              result <- allUsers
            } yield assert(result)(hasSameElements(expected)))
              .provideLayer(UserService.live(users.toM) ++ env)
          }.provideLayer(testEnvironment ++ IdGenerator.live)
        }
      )
    )

  implicit class UserOps(val users: List[User]) extends AnyVal {
    def toM = users.map(u => (u.id, u)).toMap
  }
}
