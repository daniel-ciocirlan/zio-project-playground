package controllers

import domain.errors.HttpError
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
import zio.Task

/** A base trait that all Controller implementations should extend.
  */
trait BaseController {

  /** A collection of routes that will be aggregated, and served in
    * [[_root_.Main]]. Since we should be building our Controller
    * implementations with the service module pattern, our R should be fixed at
    * Any here, and we won't have to worry about widening any Types when
    * aggregating.
    */
  val routes: List[ServerEndpoint[Any, Task]] = List.empty

  /** A base tapir endpoint that sets up consistent Error messaging.
    */
  val baseEndpoint: Endpoint[Unit, Unit, Throwable, Unit, Any] =
    endpoint
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode _)(HttpError.encode)

}
