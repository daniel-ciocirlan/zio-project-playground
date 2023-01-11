package domain.api.response

import upickle.default._

case class Company(
    id: Long,
    slug: String,
    name: String,
    url: String
)

object Company {
  implicit val rw: ReadWriter[Company] = macroRW
}
