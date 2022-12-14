package services.user

import com.dimafeng.testcontainers.PostgreSQLContainer
import domain.errors.DatabaseError
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repositories.Repository
import repositories.users.UserRepositoryLive
import services.flyway.{FlywayService, FlywayServiceLive}
import services.jwt.{JWTService, JWTServiceLive}
import zio._
import zio.test.{assertTrue, Spec, TestAspect, TestEnvironment, ZIOSpecDefault}

import javax.sql.DataSource

object UserServiceITSpec extends ZIOSpecDefault {

  val userName: String       = "alteartionx10"
  val firstPassword: String  = "abc123"
  val secondPassword: String = "babyunme"

  val tests: Spec[FlywayService with UserService with JWTService, Object] =
    suite("UserService")(
      test("register")(
        for {
          insert <- ZIO.serviceWithZIO[UserService](
                      _.registerUser(userName, firstPassword)
                    )
          fail   <- ZIO
                      .serviceWithZIO[UserService](
                        _.registerUser(userName, firstPassword)
                      )
                      .flip
        } yield assertTrue(
          insert.userName == userName,
          fail.isInstanceOf[DatabaseError]
        )
      ),
      test("verify password")(
        for {
          pass <- ZIO.serviceWithZIO[UserService](
                    _.verifyPassword(userName, firstPassword)
                  )
          fail <- ZIO.serviceWithZIO[UserService](
                    _.verifyPassword(userName, secondPassword)
                  )
        } yield assertTrue(pass, !fail)
      ),
      test("update password")(
        for {
          error   <-
            ZIO
              .serviceWithZIO[UserService](
                _.updatePassword(userName, secondPassword, secondPassword)
              )
              .flip
          updated <- ZIO.serviceWithZIO[UserService](
                       _.updatePassword(userName, firstPassword, secondPassword)
                     )
          pass    <- ZIO.serviceWithZIO[UserService](
                       _.verifyPassword(userName, secondPassword)
                     )
          fail    <- ZIO.serviceWithZIO[UserService](
                       _.verifyPassword(userName, firstPassword)
                     )
        } yield assertTrue(
          error.isInstanceOf[DatabaseError],
          updated.userName == userName,
          pass,
          !fail
        )
      ),
      test("delete account")(
        for {
          fail   <- ZIO
                      .serviceWithZIO[UserService](
                        _.deleteAccount(userName, firstPassword)
                      )
                      .flip
          result <- ZIO.serviceWithZIO[UserService](
                      _.deleteAccount(userName, secondPassword)
                    )
        } yield assertTrue(
          fail.isInstanceOf[DatabaseError],
          result.userName == userName
        )
      ),
      test("generate valid token") {
        for {
          insert    <- ZIO.serviceWithZIO[UserService](
                         _.registerUser(userName, firstPassword)
                       )
          token     <- ZIO
                         .serviceWithZIO[UserService](
                           _.generateToken(userName, firstPassword)
                         )
                         .someOrFail(new Exception("could verify inserted user"))
          validated <-
            ZIO.serviceWithZIO[JWTService](_.verifyToken(token.accessToken))
        } yield assertTrue(validated.userName == insert.userName)
      }
    ) @@ TestAspect.sequential @@ TestAspect.beforeAll(
      ZIO
        .serviceWithZIO[FlywayService](_.runMigrations)
    )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserServiceITSpec")(
      tests
    ).provideSome[DataSource & PostgreSQLContainer & Scope](
      Repository.quillPostgresLayer,
      UserRepositoryLive.layer,
      UserServiceLive.layer,
      FlywayServiceLive.testContainerLayer,
      JWTServiceLive.configuredLayer
    ).provideSomeLayerShared[Scope](
      ZPostgreSQLContainer.Settings.default >>> ZPostgreSQLContainer.live
    )
}
