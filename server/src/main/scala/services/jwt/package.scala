package services

package object jwt {

  case class Config(
      secret: Option[String],
      ttl: Int
  )

  case class ValidatedUserToken(userId: Int, userName: String)

}
