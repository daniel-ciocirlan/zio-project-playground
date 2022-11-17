package domain.api.response

import zio.json._

case class TokenResponse(
    accessToken: String
)

object TokenResponse {
  implicit val codec: JsonCodec[TokenResponse] =
    DeriveJsonCodec.gen[TokenResponse]
}
