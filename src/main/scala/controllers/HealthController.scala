package controllers

import sttp.tapir.plainBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio._

object HealthController {

  /** A ZIO constructor for our implementation (NOT a ZLayer!). See
    * [[_root_.Main.makeControllers]]
    */
  val makeZIO: UIO[HealthController] =
    ZIO.succeed(HealthController())

}

/** A Controller collecting health related endpoints.
  */
case class HealthController() extends BaseController {

  /** 200 "ok" at /health
    */
  val healthRoute: Full[Unit, Unit, Unit, Throwable, String, Any, Task] =
    baseEndpoint
      .tag("health")                        // swagger tag
      .name("Health")                       // swagger name
      .description("Health-check endpoint") // swagger description
      .get
      .in("health")
      .out(plainBody[String])
      .serverLogicSuccess(_ => ZIO.succeed("ok"))

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    healthRoute
  )

}
