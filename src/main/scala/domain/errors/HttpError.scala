package domain.errors

import sttp.model.StatusCode

final case class HttpError(
    statusCode: StatusCode,
    message: String,
    cause: Throwable
) extends ApplicationServerException(message, cause)

object HttpError {

  def decode(tuple: (StatusCode, String)): HttpError = {
    HttpError(tuple._1, tuple._2, new Exception(tuple._2))
  }

  def encode(t: Throwable): (StatusCode, String) = {
    mapException(t)
  }

  def mapException(t: Throwable): (StatusCode, String) = t match {
    case _ => (StatusCode.InternalServerError, t.getMessage)
  }
}
