package controllers

import endpoints.HealthEndpoints
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio._

import java.time.Instant

object HealthController {

  /** A ZIO constructor for our implementation (NOT a ZLayer!). See
    * [[_root_.Main.makeControllers]]
    */
  val makeZIO: UIO[HealthController] =
    ZIO.succeed(HealthController())

}

/** A Controller collecting health related endpoints.
  */
case class HealthController() extends BaseController with HealthEndpoints {

  /** 200 "ok" at /health
    */
  val healthRoute: Full[Unit, Unit, Unit, Throwable, String, Any, Task] =
    healthEndpoint
      .serverLogicSuccess(_ => ZIO.succeed("ok"))

  val timeRoute: Full[Unit, Unit, Unit, Throwable, Instant, Any, Task] =
    timeEndpoint
      .serverLogicSuccess[Task](_ => Clock.instant)

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    healthRoute,
    timeRoute
  )

}
