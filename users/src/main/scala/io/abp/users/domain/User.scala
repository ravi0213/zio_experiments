package io.abp.users.domain

import java.time.OffsetDateTime

case class User(val id: User.Id, name: String, createdAt: OffsetDateTime)
object User {
  case class Id(value: String) extends AnyVal
}
