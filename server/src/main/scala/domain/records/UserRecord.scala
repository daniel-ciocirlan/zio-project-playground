package domain.records

import domain.api.response.User

case class UserRecord(
    id: Long,
    userName: String,
    pwHash: String
)

object UserRecord {
  implicit val conversion: UserRecord => User =
    rec => User(userName = rec.userName)
}
