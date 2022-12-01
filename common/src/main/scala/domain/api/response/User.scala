package domain.api.response

import upickle.default._

case class User(userName: String)

object User {
  implicit val rw: ReadWriter[User] = macroRW
}
