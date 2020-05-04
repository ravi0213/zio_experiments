package users.services.users

import java.time.OffsetDateTime

import users.domain.User
import users.effects.idGenerator._
import users.generators._
import users.mocks._
import zio.test._
import zio.test.Assertion._
import zio.test.environment._
import zio.test.Gen._

object UserServiceSpec extends DefaultRunnableSpec {
  val fixedUserId = User.Id("user_dbe1bc85-7f06-404e-ac8c-ae6661ff2bb6")
  val fixedDateTime = OffsetDateTime.parse("2007-12-03T10:15:30+01:00")
  val env = testIdGenerator(fixedUserId) ++ testClock(fixedDateTime)

  override def spec =
    suite("UserService")(
      suite("create")(
        testM("should create a new user and return it") {
          checkM(anyString) { name =>
            val expected = User(fixedUserId, name, fixedDateTime)
            (for {
              userService <- UserService.live()
              result <- userService.create(name)
            } yield assert(result)(equalTo(expected))).provideLayer(env)
          }
        }
      ),
      suite("get")(
        testM("should return the user with the corresponding id") {
          checkM(Gen.listOfN(10)(userGen)) { users =>
            val name = "Alex"
            val expected = Some(User(fixedUserId, name, fixedDateTime))
            (for {
              userService <- UserService.live(users.toM)
              _ <- userService.create(name)
              result <- userService.get(fixedUserId)
            } yield assert(result)(equalTo(expected))).provideLayer(env)
          }.provideLayer(testEnvironment ++ testIdGenerator(fixedUserId))
        }
      ),
      suite("getByName")(
        testM("should return the list of users with the corresponding name") {
          checkM(Gen.listOfN(10)(userGen)) { users =>
            val name = "Alex"
            val expected = List(User(fixedUserId, name, fixedDateTime))
            (for {
              userService <- UserService.live(users.toM)
              _ <- userService.create(name)
              result <- userService.getByName(name)
            } yield assert(result)(equalTo(expected))).provideLayer(env)
          }.provideLayer(testEnvironment ++ testIdGenerator(fixedUserId))
        }
      ),
      suite("all")(
        testM("should return the list of all users") {
          checkM(Gen.listOfN(10)(userGen)) { users =>
            val expected = users
            for {
              userService <- UserService.live(users.toM)
              result <- userService.all
            } yield assert(result)(hasSameElements(expected))
          }.provideLayer(testEnvironment ++ IdGenerator.live)
        }
      )
    )

  implicit class UserOps(val users: List[User]) extends AnyVal {
    def toM = users.map(u => (u.id, u)).toMap
  }
}
