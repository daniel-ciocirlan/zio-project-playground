package services

package object flyway {

  case class Config(url: String, user: String, password: String)

}
