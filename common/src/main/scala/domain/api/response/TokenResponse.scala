package domain.api.response

import zio.json.{DeriveJsonCodec, JsonCodec}

case class TokenResponse(
    accessToken: String,
    user: User,
    expires: Long
)

object TokenResponse {
  implicit val codec: JsonCodec[TokenResponse] = DeriveJsonCodec.gen[TokenResponse]
}
