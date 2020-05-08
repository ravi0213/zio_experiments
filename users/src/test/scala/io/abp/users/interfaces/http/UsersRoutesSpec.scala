package io.abp.users.interfaces.http

//import cats.instances.list._
//import cats.syntax.traverse._
import io.abp.users.domain.User

//import io.abp.users.effects.idGenerator._
import io.abp.users.fixtures._
//import io.abp.users.generators._
import io.abp.users.services.users._
//import io.abp.users.services.users.{User => UserService}
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
import zio.interop.catz._
import zio.test._
import zio.test.Assertion._
//import zio.test.environment._

object UsersRoutesSpec extends DefaultRunnableSpec {
  type AppTask[A] = ZIO[Any, Throwable, A]

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

          for {
            response <- UsersRoutes(envs).routes.orNotFound(createUserRequest)
            result <- response.as[User.Id]
          } yield assert(result)(equalTo(expected.id))
        }
      )
      //suite("GET /users route")(
      //  testM("should return all existing users") {
      //    val postRequest = Request[AppTask](uri = uri"/users", method = POST)
      //    val getRequest = Request[AppTask](uri = uri"/users", method = GET)
      //    //TODO: FIX flakey tests
      //    // - only works once because the User interpreter is fixed
      //    // - only works with low values of listOfN
      //    checkNM(1)(Gen.listOfN(3)(userGen)) {
      //      users =>
      //        val envs =
      //          TestEnvironments(testIdGenerator = IdGenerator.live, testUserService = UserService.inMemory())
      //        val userRoutes =
      //          UsersRoutes(envs).routes
      //        println("ONCE")
      //        val expected = users

      //        val createUsers =
      //          users
      //            .map(user => Map(("name" -> user.name)))
      //            .traverse { body =>
      //              val createUserRequest = postRequest.withEntity(body.asJson)
      //              userRoutes.orNotFound(createUserRequest)
      //            }

      //        val getAllUsers = userRoutes.orNotFound(getRequest)

      //        for {
      //          _ <- createUsers
      //          response <- getAllUsers
      //          result <- response.as[List[User]]
      //          _ <- ZIO.effectTotal(println(result.size))
      //        } yield assert(result.map(_.name))(equalTo(expected.map(_.name)))
      //    }.provideLayer(testEnvironment ++ IdGenerator.live)
      //  }
      //)
    )
}
