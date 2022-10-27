package domain.records

import zio.json.{DeriveJsonCodec, JsonCodec}

case class LocationRecord(
    country: String,
    city: String
)

object LocationRecord {

  implicit val codec: JsonCodec[LocationRecord] =
    DeriveJsonCodec.gen[LocationRecord]

}
