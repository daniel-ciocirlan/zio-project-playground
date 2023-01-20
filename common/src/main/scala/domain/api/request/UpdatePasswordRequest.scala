package domain.api.request

import zio.json.{DeriveJsonCodec, JsonCodec}

case class UpdatePasswordRequest(
    userName: String,
    oldPassword: String,
    newPassword: String
)

object UpdatePasswordRequest {
  implicit lazy val codec: JsonCodec[UpdatePasswordRequest] = DeriveJsonCodec.gen[UpdatePasswordRequest]
}
