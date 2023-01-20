package domain.api.request

import zio.json.{DeriveJsonCodec, JsonCodec}

case class RegisterAccountRequest(
    userName: String,
    password: String
)

object RegisterAccountRequest {
  implicit lazy val codec: JsonCodec[RegisterAccountRequest] = DeriveJsonCodec.gen[RegisterAccountRequest]
}
