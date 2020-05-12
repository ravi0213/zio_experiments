package io.abp.users

import java.time.OffsetDateTime

import io.abp.users.domain.User

package object fixtures {
  val fixedName = "Alex"
  val fixedUserId = User.Id("user_dbe1bc85-7f06-404e-ac8c-ae6661ff2bb6")
  val fixedDateTime = OffsetDateTime.parse("2007-12-03T10:15:30+01:00")

  val user = User(fixedUserId, fixedName, fixedDateTime)
}
