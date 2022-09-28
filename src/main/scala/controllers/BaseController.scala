package controllers

import domain.errors.HttpError
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
import zio.Task

trait BaseController {

  val routes: List[ServerEndpoint[Any, Task]] = List.empty

  val baseEndpoint: Endpoint[Unit, Unit, Throwable, Unit, Any] =
    endpoint
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode _)(HttpError.encode)

}
