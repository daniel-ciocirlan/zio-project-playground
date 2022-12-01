package domain.api.request

import upickle.default._

case class DeleteAccountRequest(
    userName: String,
    password: String
)

object DeleteAccountRequest {
  implicit val rw: ReadWriter[DeleteAccountRequest] = macroRW

}
