package services.jwt

import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.{JWT, JWTVerifier}
import domain.records.UserRecord
import services.config.ConfigService
import zio.{Clock, Task, ZIO, ZLayer}

import java.security.SecureRandom

trait JWTService {
  def createToken(userRecord: UserRecord): Task[String]
  def verifyToken(token: String): Task[ValidatedUserToken]
}

case class JWTServiceLive(config: JwtConfig, javaClock: java.time.Clock)
    extends JWTService {

  private final val algorithm: Algorithm = {
    config.secret.fold {
      val random = new SecureRandom()
      val secret = (1 to 32)
        .map { _ =>
          (random.nextInt(75) + 48).toChar
        }
        .mkString
        .replaceAll("\\\\+", "/")
      Algorithm.HMAC512(secret)

    }(secret => Algorithm.HMAC512(secret))
  }

  private final val verifier: JWTVerifier =
    JWT
      .require(algorithm)
      .withIssuer("RTJVM")
      .asInstanceOf[BaseVerification]
      .build(javaClock)

  // https://www.iana.org/assignments/jwt/jwt.xhtml
  override def createToken(userRecord: UserRecord): Task[String] = {
    for {
      now   <- Clock.instant
      token <- ZIO.attempt {
                 JWT
                   .create()
                   .withIssuer("RTJVM")
                   .withIssuedAt(now)
                   .withExpiresAt(now.plusSeconds(config.ttl))
                   .withSubject(userRecord.id.toString)
                   .withClaim("preferred_username", userRecord.userName)
                   .sign(algorithm)
               }
    } yield token

  }

  override def verifyToken(token: String): Task[ValidatedUserToken] = {
    for {
      decoded <- ZIO.attempt(verifier.verify(token))
      token   <- ZIO.attempt(
                   ValidatedUserToken(
                     decoded.getSubject.toInt,
                     decoded.getClaim("preferred_username").asString()
                   )
                 )
    } yield token
  }
}

object JWTServiceLive {

  val layer: ZLayer[JwtConfig, Nothing, JWTService] = ZLayer {
    for {
      config <- ZIO.service[JwtConfig]
      clock  <- Clock.javaClock
    } yield JWTServiceLive(config, clock)
  }

  val configuredLayer: ZLayer[Any, Throwable, JWTService] =
    ConfigService.makeConfig[JwtConfig]("rock.the.jvm.jwt") >>> layer

}
