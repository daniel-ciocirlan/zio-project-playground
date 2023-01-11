package services

package object jwt {

  case class JwtConfig(
      secret: Option[String],
      ttl: Int
  )

  case class ValidatedUserToken(userId: Long, userName: String)

}
