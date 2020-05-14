package io.abp.users.interfaces.http

import cats.instances.list._
import cats.syntax.traverse._
import io.abp.users.domain.User
import io.abp.users.effects.idGenerator._
import io.abp.users.fixtures._
import io.abp.users.generators._
import io.abp.users.services.users
import io.abp.users.services.users.{User => UserService}
import io.abp.users.TestEnvironments
import io.abp.users.utils._
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
  type AppTask[A] = RIO[Env with users.UserService[Env], A]

  def makeUserService(input: Ref[Map[User.Id, User]]) = UserService.inMemory(input)

  implicit val circeConfig: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults
  implicit val userIdDecoder: Decoder[User.Id] = deriveConfiguredDecoder[User.Id]
  implicit val userDecoder: Decoder[User] = deriveConfiguredDecoder[User]
  implicit val allUsersResponseDecoder: Decoder[AllUsersResponse] = deriveConfiguredDecoder[AllUsersResponse]
  implicit val createUserResponseDecoder: Decoder[CreateUserResponse] =
    deriveConfiguredDecoder[CreateUserResponse]
  implicit val getUserResponseDecoder: Decoder[GetUserResponse] = deriveConfiguredDecoder[GetUserResponse]
  case class AllUsersResponse(users: List[User])
  case class CreateUserResponse(id: User.Id)
  case class GetUserResponse(user: User)

  override def spec =
    suite("UsersRoutes")(
      suite("POST /users route")(
        testM("should create a new users and return it") {
          val envs = TestEnvironments()
          val body = Map(("name" -> fixedName))
          val createUserRequest = Request[AppTask](uri = uri"/users", method = POST).withEntity(body.asJson)
          val expected = user
          Ref.make(Map.empty[User.Id, User]).flatMap { ref =>
            (for {
              response <- UsersRoutes[Env].routes.orNotFound(createUserRequest)
              result <- response.as[CreateUserResponse]
            } yield assert(result.id)(equalTo(expected.id)))
              .provideLayer(envs.env ++ ZLayer.succeed(makeUserService(ref)))
          }
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

              Ref.make(Map.empty[User.Id, User]).flatMap {
                ref =>
                  (for {
                    userRoutes <- ZIO.succeed(UsersRoutes[Env].routes)
                    _ <-
                      users
                        .map(user => Map(("name" -> user.name)))
                        .traverse(body => userRoutes.orNotFound(postRequest.withEntity(body.asJson)))
                    response <- userRoutes.orNotFound(getRequest)
                    result <- response.as[AllUsersResponse]
                  } yield assert(result.users.map(_.name))(hasSameElements(expected.map(_.name))))
                    .provideLayer(envs.env ++ ZLayer.succeed(makeUserService(ref)))
              }
          }.provideLayer(testEnvironment ++ IdGenerator.live)
        }
      ),
      suite("GET /users/:id route")(
        testM("should return user with specified id") {
          val getRequest =
            (id: User.Id) => Request[AppTask](uri = Uri(path = s"/users/${id.value}"), method = GET)
          checkM(Gen.listOfN(10)(userGen)) {
            users =>
              val expected = user
              val envs = TestEnvironments(testIdGenerator = IdGenerator.live)

              Ref.make((user +: users).toM).flatMap { ref =>
                (for {
                  userRoutes <- ZIO.succeed(UsersRoutes[Env].routes)
                  response <- userRoutes.orNotFound(getRequest(user.id))
                  result <- response.as[GetUserResponse]
                } yield assert(result.user)(equalTo(expected)))
                  .provideLayer(envs.env ++ ZLayer.succeed(makeUserService(ref)))
              }
          }.provideLayer(testEnvironment ++ IdGenerator.live)
        }
      )
    )
}
