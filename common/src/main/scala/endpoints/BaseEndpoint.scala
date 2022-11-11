package endpoints

import domain.errors.HttpError
import sttp.tapir._

/** A base trait that all Controller implementations should extend.
  */
trait BaseEndpoint {

  /** A base tapir endpoint that sets up consistent Error messaging.
    */
  val baseEndpoint: Endpoint[Unit, Unit, Throwable, Unit, Any] =
    endpoint
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode _)(HttpError.encode)

}
