package domain.api.request

import upickle.default._

case class UpdatePasswordRequest(
    userName: String,
    oldPassword: String,
    newPassword: String
)

object UpdatePasswordRequest {
  implicit val rw: ReadWriter[UpdatePasswordRequest] = macroRW

}
