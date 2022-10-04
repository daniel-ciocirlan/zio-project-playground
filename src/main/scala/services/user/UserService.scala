package services.user

import domain.api.User
import domain.errors.DatabaseError
import domain.records.UserRecord
import extensions.Conversions.ConversionExtension
import repositories.users.UserRepository
import zio._

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

trait UserService {

  def registerUser(userName: String, password: String): Task[User]
  def verifyPassword(userName: String, password: String): Task[Boolean]
  def updatePassword(
      userName: String,
      oldPassword: String,
      newPassword: String
  ): Task[User]
  def deleteAccount(userName: String, password: String): Task[User]

}

case class UserServiceLive(userRepository: UserRepository) extends UserService {

  private val PBKDF2_ALGORITHM: String = "PBKDF2WithHmacSHA512"
  private val SALT_BYTE_SIZE: Int      = 24
  private val HASH_BYTE_SIZE: Int      = 24
  private val PBKDF2_ITERATIONS: Int   = 1000
  private val ITERATION_INDEX: Int     = 0
  private val SALT_INDEX: Int          = 1
  private val PBKDF2_INDEX: Int        = 2

  val skf: SecretKeyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)

  private def slowEquals(a: Array[Byte], b: Array[Byte]): Boolean = {
    val range = 0 until math.min(a.length, b.length)
    val diff  = range.foldLeft(a.length ^ b.length) { case (acc, i) =>
      acc | a(i) ^ b(i)
    }
    diff == 0
  }

  private def pbkdf2(
      message: Array[Char],
      salt: Array[Byte],
      iterations: Int,
      bytes: Int
  ): Array[Byte] = {
    val keySpec: PBEKeySpec =
      new PBEKeySpec(message, salt, iterations, bytes * 8)
    skf.generateSecret(keySpec).getEncoded
  }

  private def fromHex(hex: String): Array[Byte] = {
    hex.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  private def toHex(array: Array[Byte]): String = {
    array.map("%02X" format _).mkString
  }

  /** Creates a PBKDF2 hash
    *
    * @param str
    * @return
    *   A hash of the form nIteration:salt:hash where salt and hash are in hex
    *   form
    */
  def pbkdf2Hash(str: String, iterations: Int = PBKDF2_ITERATIONS): String = {

    val rng: SecureRandom = new SecureRandom()
    val salt: Array[Byte] = Array.ofDim[Byte](SALT_BYTE_SIZE)
    rng.nextBytes(salt)
    val hashBytes         = pbkdf2(str.toCharArray, salt, iterations, HASH_BYTE_SIZE)
    s"$iterations:${toHex(salt)}:${toHex(hashBytes)}"

  }

  /** Validates a PBKDF2 hash
    *
    * @param str
    *   The plain text you are confirming
    * @param hash
    *   The hash, in form of nIteration:salt:hash
    * @return
    */
  def validatePbkdf2Hash(str: String, hash: String): Boolean = {
    val hashSegments   = hash.split(":")
    val validHash      = fromHex(hashSegments(PBKDF2_INDEX))
    val hashIterations = hashSegments(ITERATION_INDEX).toInt
    val hashSalt       = fromHex(hashSegments(SALT_INDEX))
    val testHash       =
      pbkdf2(str.toCharArray, hashSalt, hashIterations, HASH_BYTE_SIZE)
    slowEquals(validHash, testHash)
  }

  override def registerUser(userName: String, password: String): Task[User] =
    for {
      existingUser <- userRepository.getByUserName(userName)
      _            <-
        ZIO.cond(
          existingUser.isEmpty,
          (),
          DatabaseError("Cannot create account. Username already exists.", null)
        )
      user         <- userRepository
                        .create(
                          UserRecord(
                            id = -1,
                            userName = userName,
                            pwHash = pbkdf2Hash(password)
                          )
                        )
                        .map(_.to[User])
    } yield user

  override def verifyPassword(
      userName: String,
      password: String
  ): Task[Boolean] =
    for {
      existingUser <- userRepository
                        .getByUserName(userName)
                        .someOrFail(DatabaseError("User does not exist", null))
      result       <- ZIO.attempt(validatePbkdf2Hash(password, existingUser.pwHash))
    } yield result

  override def updatePassword(
      userName: String,
      oldPassword: String,
      newPassword: String
  ): Task[User] = {
    for {
      existingUser <- userRepository
                        .getByUserName(userName)
                        .someOrFail(DatabaseError("User does not exist", null))
      verified     <-
        ZIO.attempt(validatePbkdf2Hash(oldPassword, existingUser.pwHash))
      result       <-
        userRepository
          .update(
            existingUser.id,
            rec => rec.copy(pwHash = pbkdf2Hash(newPassword))
          )
          .map(_.to[User])
          .when(verified)
          .someOrFail(
            DatabaseError(
              s"Could not update password for account $userName",
              null
            )
          )
    } yield result
  }

  override def deleteAccount(
      userName: String,
      password: String
  ): Task[User] = {
    for {
      existingUser <- userRepository
                        .getByUserName(userName)
                        .someOrFail(DatabaseError("User does not exist", null))
      verified     <-
        ZIO.attempt(validatePbkdf2Hash(password, existingUser.pwHash))
      result       <-
        userRepository
          .delete(existingUser.id)
          .when(verified)
          .someOrFail(
            DatabaseError(s"Could not delete account for $userName.", null)
          )
          .map(_.to[User])
    } yield result
  }
}

object UserServiceLive {

  val layer: ZLayer[UserRepository, Nothing, UserService] = ZLayer {
    for {
      repo <- ZIO.service[UserRepository]
    } yield UserServiceLive(repo)
  }

}
