package domain.errors

sealed abstract class ApplicationError(message: String, cause: Throwable)
    extends Exception(message, cause)

abstract class DatabaseException(message: String, cause: Throwable)
    extends ApplicationError(message, cause)

abstract class ApplicationServerException(message: String, cause: Throwable)
    extends ApplicationError(message, cause)

final case class UnknownException(message: String, cause: Throwable)
    extends ApplicationError(message, cause)
