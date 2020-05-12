package io.abp.users.services.users

import io.abp.users.domain.User
import io.abp.users.effects.idGenerator._
import io.abp.users.fixtures._
import io.abp.users.generators._
import io.abp.users.mocks._
import io.abp.users.services.users.{User => UserService}
import io.abp.users.TestEnvironments
import io.abp.users.utils._
import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.environment._
import zio.test.Gen._

object UserServiceSpec extends DefaultRunnableSpec {
  def makeUserService(input: Ref[Map[User.Id, User]]) = UserService.inMemory(input)

  override def spec =
    suite("UserService")(
      suite("create")(
        testM("should create a new user and return it") {
          for {
            ref <- Ref.make(Map.empty[User.Id, User])
            userService = makeUserService(ref)
            result <- checkM(anyString.noShrink) { name =>
              val expected = user.copy(name = name)
              userService
                .create(name)
                .map { result => assert(result)(equalTo(expected)) }
                .provideLayer(TestEnvironments().env)
            }
          } yield result
        }
      ),
      suite("get")(
        testM("should return the user with the corresponding id") {
          checkM(Gen.listOfN(10)(userGen)) { users =>
            val expected = Some(user)
            (for {
              ref <- Ref.make(users.toM)
              userService = makeUserService(ref)
              _ <- userService.create(fixedName)
              result <- userService.get(fixedUserId)
            } yield assert(result)(equalTo(expected)))
              .provideLayer(TestEnvironments().env)
          }.provideLayer(testEnvironment ++ testIdGeneratorMock(fixedUserId))
        }
      ),
      suite("getByName")(
        testM("should return the list of users with the corresponding name") {
          checkM(Gen.listOfN(10)(userGen)) { users =>
            val expected = List(user)
            (for {
              ref <- Ref.make(users.toM)
              userService = makeUserService(ref)
              _ <- userService.create(fixedName)
              result <- userService.getByName(fixedName)
            } yield assert(result)(equalTo(expected)))
              .provideLayer(TestEnvironments().env)
          }.provideLayer(testEnvironment ++ testIdGeneratorMock(fixedUserId))
        }
      ),
      suite("all")(
        testM("should return the list of all users") {
          checkM(Gen.listOfN(10)(userGen)) { users =>
            (for {
              ref <- Ref.make(users.toM)
              userService = makeUserService(ref)
              user <- userService.create(fixedName)
              expected <- ZIO.succeed(user +: users)
              result <- userService.all
            } yield assert(result)(hasSameElements(expected)))
              .provideLayer(
                TestEnvironments(
                  testIdGenerator = IdGenerator.live
                ).env
              )
          }.provideLayer(testEnvironment ++ IdGenerator.live)
        }
      )
    )
}
