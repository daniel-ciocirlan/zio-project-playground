package domain.errors

import sttp.model.StatusCode

/** An [[domain.errors.ApplicationServerException]] error that is used to
  * encode/decode errors via tapir
  * @param statusCode
  * @param message
  * @param cause
  */
final case class HttpError(
    statusCode: StatusCode,
    message: String,
    cause: Throwable
) extends ApplicationServerException(message, cause)

case object UnauthorizedException
    extends ApplicationServerException("Unauthorized", null)

object HttpError {

  /** A method used via tapir to decode a response tuple to an [[HttpError]]
    * @param tuple
    * @return
    */
  def decode(tuple: (StatusCode, String)): HttpError = {
    HttpError(tuple._1, tuple._2, new Exception(tuple._2))
  }

  /** A method used via tapir to encode an error to a response tuple
    * @param t
    * @return
    */
  def encode(t: Throwable): (StatusCode, String) = {
    mapException(t)
  }

  /** A central location to match/map our custom errors to error response tuples
    * @param t
    * @return
    */
  def mapException(t: Throwable): (StatusCode, String) = t match {
    case UnauthorizedException => (StatusCode.Unauthorized, t.getMessage)
    case _                     => (StatusCode.InternalServerError, t.getMessage)
  }
}
