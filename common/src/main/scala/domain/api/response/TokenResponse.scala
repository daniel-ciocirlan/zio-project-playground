package domain.api.response

import upickle.default._

case class TokenResponse(
    accessToken: String
)

object TokenResponse {
  implicit val rw: ReadWriter[TokenResponse] = macroRW
}
