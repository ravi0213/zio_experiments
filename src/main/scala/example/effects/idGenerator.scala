package example.effects

import example.domain.User
import zio._
import zio.random._

package object idGenerator {
  type IdGenerator = Has[IdGenerator.Service]

  object IdGenerator {
    trait Service {
      def userId: UIO[User.Id]
    }

    object Service {
      val live: ZLayer[Random, Nothing, IdGenerator] =
        ZLayer.fromFunction(random =>
          new Service {
            val userId = random.get.nextLong(1000000000).map(id => User.Id(s"user_$id"))
          }
        )
    }

    val live: ZLayer[Any, Nothing, IdGenerator] =
      Random.live >>> Service.live
  }

  val userId: ZIO[IdGenerator, Nothing, User.Id] =
    ZIO.accessM(_.get.userId)
}
