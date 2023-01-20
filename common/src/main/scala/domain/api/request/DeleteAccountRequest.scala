package domain.api.request

import zio.json.{DeriveJsonCodec, JsonCodec}

case class DeleteAccountRequest(
    userName: String,
    password: String
)

object DeleteAccountRequest {
  implicit lazy val codec: JsonCodec[DeleteAccountRequest] = DeriveJsonCodec.gen[DeleteAccountRequest]
}
