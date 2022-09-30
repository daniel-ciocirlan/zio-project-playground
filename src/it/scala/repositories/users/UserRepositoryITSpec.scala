package repositories.users

import domain.records.UserRecord
import repositories.Repository
import services.flyway.{FlywayService, FlywayServiceLive}
import zio.Scope
import zio.test._
import zio._

// NOTE - for now, will need to docker-compose down/up between runs to clear database.
// WIll add some auto clean-up functionality (or test-containers) later
object UserRepositoryITSpec extends ZIOSpecDefault {

  val someUser: UserRecord =
    UserRecord(-1, "alterationx10", "supersecretpwhash")

  val tests: Spec[UserRepository, Throwable] = suite("UserRepository")(
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
  )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserRepositoryITSpec")(
      tests
    ).provide(
      Repository.dataSourceLayer,
      Repository.quillPostgresLayer,
      UserRepositoryLive.layer
    ) @@ TestAspect.sequential @@ TestAspect.beforeAll(
      ZIO
        .serviceWithZIO[FlywayService](_.runMigrations)
        .provideLayer(FlywayServiceLive.layer)
    )
}
