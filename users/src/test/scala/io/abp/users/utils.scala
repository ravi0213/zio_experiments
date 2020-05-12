package io.abp.users

import io.abp.users.domain.User

package object utils {

  implicit class UserOps(val users: List[User]) extends AnyVal {
    def toM = users.map(u => (u.id, u)).toMap
  }

}
