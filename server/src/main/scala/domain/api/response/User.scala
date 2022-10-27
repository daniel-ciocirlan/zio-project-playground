package domain.api.response

import zio.json.{DeriveJsonCodec, JsonCodec}

case class User(userName: String)

object User {
  implicit val codec: JsonCodec[User] = DeriveJsonCodec.gen[User]
}
