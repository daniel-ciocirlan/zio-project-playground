package domain.api.response

import upickle.default._

case class Company ()

object Company {
  implicit val rw: ReadWriter[Company] = macroRW
}