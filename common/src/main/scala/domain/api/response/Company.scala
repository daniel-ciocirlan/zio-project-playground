package domain.api.response

import zio.json.{DeriveJsonCodec, JsonCodec}

case class Company(
    id: Long,
    slug: String,
    name: String,
    url: String
)

object Company {
  implicit val codec: JsonCodec[Company] = DeriveJsonCodec.gen[Company]
}
