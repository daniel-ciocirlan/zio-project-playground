package domain.api.request

import upickle.default._

case class RegisterAccountRequest(
    userName: String,
    password: String
)

object RegisterAccountRequest {
  implicit val rw: ReadWriter[RegisterAccountRequest] = macroRW

}
