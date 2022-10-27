package repositories.companies

import domain.records.{CompanyRecord, LocationRecord}
import io.getquill.JsonValue
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repositories.Repository
import services.flyway.{FlywayService, FlywayServiceLive}
import zio._
import zio.test._

import java.sql.SQLException

object CompanyRepositoryITSpec extends ZIOSpecDefault {

  val someCompany: CompanyRecord =
    CompanyRecord(
      id = -1,
      slug = "rockthejvm",
      name = "Rock the JVM",
      hq = Option(JsonValue(LocationRecord("RO", "Bucharest"))),
      offices = JsonValue(Seq.empty)
    )

  val tests = suite("CompanyRepository")(
    test("create") {
      for {
        _   <- ZIO.serviceWithZIO[CompanyRepository](_.create(someCompany))
        err <- ZIO.serviceWithZIO[CompanyRepository](_.create(someCompany)).flip
      } yield assertTrue(err.isInstanceOf[SQLException])
    },
    test("getBy") {
      for {
        created       <- ZIO.serviceWithZIO[CompanyRepository](
                           _.create(someCompany.copy(slug = "rtjvm"))
                         )
        fetchedById   <-
          ZIO.serviceWithZIO[CompanyRepository](_.getById(created.id))
        fetchedBySlug <-
          ZIO.serviceWithZIO[CompanyRepository](_.getBySlug("rtjvm"))
      } yield assertTrue(
        fetchedById.contains(created),
        fetchedBySlug.contains(created)
      )
    },
    test("updated") {
      for {
        created     <- ZIO.serviceWithZIO[CompanyRepository](
                         _.create(someCompany.copy(slug = "rjvm"))
                       )
        updated     <-
          ZIO.serviceWithZIO[CompanyRepository](
            _.update(
              created.id,
              rec =>
                rec.copy(offices =
                  JsonValue(
                    rec.offices.value :+ LocationRecord("US", "Buffalo")
                  )
                )
            )
          )
        fetchedById <-
          ZIO.serviceWithZIO[CompanyRepository](_.getById(created.id))
      } yield assertTrue(
        fetchedById.contains(updated)
      )
    },
    test("delete") {
      for {
        created     <- ZIO.serviceWithZIO[CompanyRepository](
                         _.create(someCompany.copy(slug = "rtjayveeem"))
                       )
        _           <- ZIO.serviceWithZIO[CompanyRepository](_.delete(created.id))
        fetchedById <-
          ZIO.serviceWithZIO[CompanyRepository](_.getById(created.id))
      } yield assertTrue(fetchedById.isEmpty)
    }
  ) @@ TestAspect.beforeAll(
    ZIO
      .serviceWithZIO[FlywayService](_.runMigrations)
  )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyRepositoryITSpec")(
      tests
    ).provideSomeShared[Scope](
      Repository.quillPostgresLayer,
      CompanyRepositoryLive.layer,
      FlywayServiceLive.testContainerLayer,
      ZPostgreSQLContainer.Settings.default >>> ZPostgreSQLContainer.live
    )
}
