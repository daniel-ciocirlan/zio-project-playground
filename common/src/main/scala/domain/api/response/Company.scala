package domain.api.response

import zio.json.{DeriveJsonCodec, JsonCodec}

case class Company ()

object Company {
  implicit val codec: JsonCodec[Company] = DeriveJsonCodec.gen[Company]
}