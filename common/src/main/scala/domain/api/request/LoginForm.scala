package domain.api.request

import zio.json.{DeriveJsonCodec, JsonCodec}

case class LoginForm(username: String, password: String)

object LoginForm {
  implicit val codec: JsonCodec[LoginForm] = DeriveJsonCodec.gen[LoginForm]
}
