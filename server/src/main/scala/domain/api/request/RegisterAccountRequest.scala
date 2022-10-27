package domain.api.request

import zio.json.{DeriveJsonCodec, JsonCodec}

case class RegisterRequest(
    userName: String,
    password: String
)

object RegisterRequest {
  implicit val codec: JsonCodec[RegisterRequest] =
    DeriveJsonCodec.gen[RegisterRequest]
}
