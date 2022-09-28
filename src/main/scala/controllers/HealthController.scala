package controllers

import sttp.tapir.plainBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio._

object HealthController {

  val makeZIO: UIO[HealthController] =
    ZIO.succeed(HealthController())

}

case class HealthController() extends BaseController {

  val healthRoute: Full[Unit, Unit, Unit, Throwable, String, Any, Task] =
    baseEndpoint
      .tag("health")
      .name("Health")
      .description("Health-check endpoint")
      .get
      .in("health")
      .out(plainBody[String])
      .serverLogicSuccess(_ => ZIO.succeed("ok"))

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    healthRoute
  )

}
