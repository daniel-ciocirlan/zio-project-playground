package repositories.companies

import domain.records.CompanyRecord
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repositories.Repository
import services.flyway.{FlywayService, FlywayServiceLive}
import zio._
import zio.test._

import java.sql.SQLException

object CompanyRepositoryITSpec extends ZIOSpecDefault {

  def rndStr(len: Int = 5): String =
    scala.util.Random.alphanumeric.take(len).mkString

  def genCompany: Iterator[CompanyRecord] = LazyList
    .continually(
      CompanyRecord(
        id = -1,
        slug = rndStr(),
        name = rndStr(),
        url = rndStr()
      )
    )
    .iterator

  val tests = suite("CompanyRepository")(
    test("create") {
      for {
        someCompany <-
          ZIO.serviceWithZIO[CompanyRepository](_.create(genCompany.next()))
        err         <- ZIO.serviceWithZIO[CompanyRepository](_.create(someCompany)).flip
      } yield assertTrue(err.isInstanceOf[SQLException])
    },
    test("get all") {
      for {
        _     <-
          ZIO.foreachDiscard(1 to 10) { _ =>
            ZIO.serviceWithZIO[CompanyRepository](_.create(genCompany.next()))
          }
        fetch <- ZIO.serviceWithZIO[CompanyRepository](_.get)
      } yield assertTrue(
        fetch.length == 10
      )
    },
    test("getBy") {
      for {
        created       <- ZIO.serviceWithZIO[CompanyRepository](
                           _.create(genCompany.next())
                         )
        fetchedById   <-
          ZIO.serviceWithZIO[CompanyRepository](_.getById(created.id))
        fetchedBySlug <-
          ZIO.serviceWithZIO[CompanyRepository](_.getBySlug(created.slug))
      } yield assertTrue(
        fetchedById.contains(created),
        fetchedBySlug.contains(created)
      )
    },
    test("updated") {
      for {
        created     <- ZIO.serviceWithZIO[CompanyRepository](
                         _.create(genCompany.next())
                       )
        updated     <-
          ZIO.serviceWithZIO[CompanyRepository](
            _.update(
              created.id,
              rec =>
                rec.copy(
                  url = "https://blog.rockthejvm.com"
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
                         _.create(genCompany.next())
                       )
        _           <- ZIO.serviceWithZIO[CompanyRepository](_.delete(created.id))
        fetchedById <-
          ZIO.serviceWithZIO[CompanyRepository](_.getById(created.id))
      } yield assertTrue(fetchedById.isEmpty)
    }
  ) @@ TestAspect.before(
    ZIO
      .serviceWithZIO[FlywayService](_.runMigrations)
  )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyRepositoryITSpec")(
      tests
    ).provideSome[Scope](
      Repository.quillPostgresLayer,
      CompanyRepositoryLive.layer,
      FlywayServiceLive.testContainerLayer,
      ZPostgreSQLContainer.Settings.default >>> ZPostgreSQLContainer.live
    )
}
