package services.jwt

import com.auth0.jwt.exceptions.TokenExpiredException
import domain.records.UserRecord
import zio._
import zio.test._

object JWTServiceSpec extends ZIOSpecDefault {

  val someUser: UserRecord =
    UserRecord(id = 123, userName = "alterationx10", pwHash = "???")

  val JWTService: ZIO.ServiceWithZIOPartiallyApplied[JWTService] =
    ZIO.serviceWithZIO[JWTService]

  val tokenTtl           = 10
  val someConfig: Config = Config(secret = Option.empty, ttl = tokenTtl)

  val tests = suite("JWTService")(
    test("generate / validate token") {
      for {
        jwt      <- JWTService(_.createToken(someUser))
        verified <- JWTService(_.verifyToken(jwt))
      } yield assertTrue(
        verified.userId == someUser.id,
        verified.userName == someUser.userName
      )
    },
    test("expires token") {
      for {
        jwt   <- JWTService(_.createToken(someUser))
        _     <- JWTService(_.verifyToken(jwt))
        _     <- TestClock.adjust((tokenTtl - 1).seconds)
        _     <- JWTService(_.verifyToken(jwt))
        _     <- TestClock.adjust(2.seconds)
        error <- JWTService(_.verifyToken(jwt)).flip
      } yield assertTrue(
        error.isInstanceOf[TokenExpiredException]
      )
    }
  )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("JWTServiceSpec")(
      tests
    ).provide(
      JWTServiceLive.layer,
      ZLayer.succeed(someConfig)
    )
}
