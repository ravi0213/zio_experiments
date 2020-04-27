package example.domain

import java.time.OffsetDateTime

case class User(id: Long, name: String, createdAt: OffsetDateTime)
