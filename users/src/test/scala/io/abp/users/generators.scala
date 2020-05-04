package io.abp.users

import java.time.OffsetDateTime

import io.abp.users.domain.User
import io.abp.users.effects.idGenerator._
import zio.random._
import zio.test._
import zio.test.Gen._

package object generators {

  val userGen: Gen[Random with Sized with IdGenerator, User] =
    for {
      id <- Gen.fromEffect(userId)
      name <- Gen.alphaNumericString
      createdAt <- offsetDateTime(
        OffsetDateTime.parse("2008-12-03T10:10:30+01:00"),
        OffsetDateTime.parse("2020-12-03T10:10:30+01:00")
      )
    } yield User(id, name, createdAt)

}
