package controllers

import endpoints.HealthEndpoints
import services.jwt.{JWTService, ValidatedUserToken}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio._

import java.time.Instant

object HealthController {

  /** A ZIO constructor for our implementation (NOT a ZLayer!). See
    * [[_root_.Main.makeControllers]]
    */
  val makeZIO: ZIO[JWTService, Nothing, HealthController] =
    for {
      jwt <- ZIO.service[JWTService]
    } yield HealthController(jwt)

}

/** A Controller collecting health related endpoints.
  */
case class HealthController(jwtService: JWTService)
    extends BaseController
    with HealthEndpoints {

  /** 200 "ok" at /health
    */
  val healthRoute: Full[Unit, Unit, Unit, Throwable, String, Any, Task] =
    healthEndpoint
      .serverLogicSuccess(_ => ZIO.succeed("ok"))

  val timeRoute: Full[Unit, Unit, Unit, Throwable, Instant, Any, Task] =
    timeEndpoint
      .serverLogicSuccess[Task](_ => Clock.instant)

  val secureTimeRoute: Full[
    String,
    ValidatedUserToken,
    Unit,
    Throwable,
    Instant,
    Any,
    Task
  ] = {
    secureTimeEndpoint
      .serverSecurityLogic[ValidatedUserToken, Task](token =>
        jwtService.verifyToken(token).either
      )
      .serverLogicSuccess(_ => _ => Clock.instant)
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    healthRoute,
    timeRoute,
    secureTimeRoute
  )

}
