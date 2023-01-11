package domain.api.request

import upickle.default._

case class CreateCompanyRequest(
    name: String,
    url: String
)

object CreateCompanyRequest{
  implicit val rw: ReadWriter[CreateCompanyRequest] = macroRW
}