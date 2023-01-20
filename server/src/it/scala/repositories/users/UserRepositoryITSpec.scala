package repositories.users

import com.dimafeng.testcontainers.PostgreSQLContainer
import domain.records.UserRecord
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repositories.Repository
import services.flyway.{FlywayService, FlywayServiceLive}
import zio.{Scope, _}
import zio.test._

import javax.sql.DataSource

// NOTE - for now, will need to docker-compose down/up between runs to clear database.
// WIll add some auto clean-up functionality (or test-containers) later
object UserRepositoryITSpec extends ZIOSpecDefault {

  val someUser: UserRecord =
    UserRecord(-1, "alterationx10", "supersecretpwhash")

  val tests: Spec[FlywayService with UserRepository, Throwable] =
    suite("UserRepository")(
      test("create")(
        for {
          _ <- ZIO.serviceWithZIO[UserRepository](_.create(someUser))
        } yield assertCompletes
      ),
      test("getById")(
        for {
          created <- ZIO.serviceWithZIO[UserRepository](_.create(someUser))
          fetched <- ZIO.serviceWithZIO[UserRepository](_.getById(created.id))
        } yield assertTrue(fetched.contains(created))
      ),
      test("update")(
        for {
          created <- ZIO
                       .serviceWithZIO[UserRepository](_.create(someUser))
          updated <-
            ZIO.serviceWithZIO[UserRepository](
              _.update(created.id, rec => rec.copy(pwHash = "uweulhwauwhae"))
            )
          fetched <- ZIO
                       .serviceWithZIO[UserRepository](_.getById(created.id))
        } yield assertTrue(fetched.contains(updated))
      ),
      test("delete")(
        for {
          created <- ZIO.serviceWithZIO[UserRepository](_.create(someUser))
          _       <- ZIO.serviceWithZIO[UserRepository](_.delete(created.id))
          fetched <- ZIO.serviceWithZIO[UserRepository](_.getById(created.id))
        } yield assertTrue(fetched.isEmpty)
      )
    ) @@ TestAspect.beforeAll(
      ZIO
        .serviceWithZIO[FlywayService](_.runMigrations)
    )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserRepositoryITSpec")(
      tests
    ).provideSome[DataSource & PostgreSQLContainer & Scope](
      Repository.quillPostgresLayer,
      UserRepositoryLive.layer,
      FlywayServiceLive.testContainerLayer
    ).provideSomeLayerShared[Scope](
      ZPostgreSQLContainer.Settings.default >>> ZPostgreSQLContainer.live
    )
}
