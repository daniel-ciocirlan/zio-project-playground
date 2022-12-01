package domain.api.request

import upickle.default._

case class LoginForm(username: String, password: String)

object LoginForm {
  implicit val rw: ReadWriter[LoginForm] = macroRW

}
