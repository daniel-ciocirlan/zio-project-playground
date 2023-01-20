package domain.api.request

import zio.json.{DeriveJsonCodec, JsonCodec}

case class CreateCompanyRequest(
    name: String,
    url: String
)

object CreateCompanyRequest{
  implicit lazy val codec: JsonCodec[CreateCompanyRequest] = DeriveJsonCodec.gen[CreateCompanyRequest]
}