package domain.errors

case class DatabaseError(message: String, cause: Throwable)
    extends DatabaseException(message, cause)
