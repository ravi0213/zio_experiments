package io.abp.users.interfaces.http

import cats.instances.list._
import cats.syntax.traverse._
import io.abp.users.domain.User
import io.abp.users.effects.idGenerator._
import io.abp.users.fixtures._
import io.abp.users.generators._
import io.abp.users.services.users._
import io.abp.users.services.users.{User => UserService}
import io.abp.users.TestEnvironments
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.io._
import org.http4s.implicits._
import zio._
import zio.clock._
import zio.interop.catz._
import zio.test._
import zio.test.Assertion._
import zio.test.environment._

object UsersRoutesSpec extends DefaultRunnableSpec {
  type Env = Clock with IdGenerator
  type AppTask[A] = ZIO[Env, Throwable, A]

  def makeUserService(input: Ref[Map[User.Id, User]]) = UserService.live(input)

  implicit val circeConfig: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults
  implicit val userIdDecoder: Decoder[User.Id] = deriveConfiguredDecoder[User.Id]
  implicit val userDecoder: Decoder[User] = deriveConfiguredDecoder[User]

  override def spec =
    suite("UsersRoutes")(
      suite("POST /users route")(
        testM("should create a new users and return it") {
          val envs = TestEnvironments()
          val name = "Alex"
          val body = Map(("name" -> name))
          val createUserRequest = Request[AppTask](uri = uri"/users", method = POST).withEntity(body.asJson)
          val expected = User(fixedUserId, name, fixedDateTime)

          (for {
            ref <- Ref.make(Map.empty[User.Id, User])
            userService = makeUserService(ref)
            response <- UsersRoutes(userService).routes.orNotFound(createUserRequest)
            result <- response.as[User.Id]
          } yield assert(result)(equalTo(expected.id))).provideLayer(envs.env)
        }
      ),
      suite("GET /users route")(
        testM("should return all existing users") {
          val postRequest = Request[AppTask](uri = uri"/users", method = POST)
          val getRequest = Request[AppTask](uri = uri"/users", method = GET)
          checkM(Gen.listOfN(10)(userGen)) {
            users =>
              val envs = TestEnvironments(testIdGenerator = IdGenerator.live)
              val expected = users

              (for {
                ref <- Ref.make(Map.empty[User.Id, User])
                userService = makeUserService(ref)
                userRoutes = UsersRoutes(userService).routes
                _ <-
                  users
                    .map(user => Map(("name" -> user.name)))
                    .traverse(body => userRoutes.orNotFound(postRequest.withEntity(body.asJson)))
                response <- userRoutes.orNotFound(getRequest)
                result <- response.as[List[User]]
              } yield assert(result.map(_.name))(hasSameElements(expected.map(_.name))))
                .provideLayer(envs.env)
          }.provideLayer(testEnvironment ++ IdGenerator.live)
        }
      )
    )
}
