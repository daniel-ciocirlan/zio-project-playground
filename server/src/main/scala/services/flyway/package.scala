package services

package object flyway {

  case class FlywayConfig(url: String, user: String, password: String)

}
