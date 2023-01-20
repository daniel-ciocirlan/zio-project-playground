package domain.api.request

import zio.json.{DeriveJsonCodec, JsonCodec}

case class DeleteAccountRequest(
    userName: String,
    password: String
)

object DeleteAccountRequest {
  implicit val codec: JsonCodec[DeleteAccountRequest] = DeriveJsonCodec.gen[DeleteAccountRequest]
}
