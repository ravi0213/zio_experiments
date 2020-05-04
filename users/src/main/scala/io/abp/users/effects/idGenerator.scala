package io.abp.users.effects

import io.abp.users.domain.User
import zio._

package object idGenerator {
  type IdGenerator = Has[IdGenerator.Service]

  object IdGenerator {
    trait Service {
      def userId: UIO[User.Id]
    }

    object Service {
      val live =
        new Service {
          val userId = UIO(java.util.UUID.randomUUID.toString).map(id => User.Id(s"user_$id"))
        }
    }

    val live: ZLayer[Any, Nothing, IdGenerator] =
      ZLayer.succeed(Service.live)
  }

  val userId: ZIO[IdGenerator, Nothing, User.Id] =
    ZIO.accessM(_.get.userId)
}
