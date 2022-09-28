package domain.errors

/** A root exception for all of our errors to extend
  * @param message
  * @param cause
  */
sealed abstract class ApplicationError(message: String, cause: Throwable)
    extends Exception(message, cause)

/** A root exception for all of our database related errors to extend
  * @param message
  * @param cause
  */
abstract class DatabaseException(message: String, cause: Throwable)
    extends ApplicationError(message, cause)

/** A root exception for all of our http-server related errors to extend
  * @param message
  * @param cause
  */
abstract class ApplicationServerException(message: String, cause: Throwable)
    extends ApplicationError(message, cause)

/** An error for when you're about to need to implement a new error class :-)
  * @param message
  * @param cause
  */
final case class UnknownException(message: String, cause: Throwable)
    extends ApplicationError(message, cause)
