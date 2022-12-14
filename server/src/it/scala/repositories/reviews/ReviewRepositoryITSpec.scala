package repositories.reviews

import domain.records.ReviewRecord
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repositories.Repository
import repositories.companies.{
  CompanyRepository,
  CompanyRepositoryITSpec,
  CompanyRepositoryLive
}
import repositories.users.{
  UserRepository,
  UserRepositoryITSpec,
  UserRepositoryLive
}
import services.flyway.{FlywayService, FlywayServiceLive}
import zio._
import zio.test._

import java.sql.SQLException
import java.time.Instant

object ReviewRepositoryITSpec extends ZIOSpecDefault {

  val someReview: ReviewRecord = {
    ReviewRecord(
      id = -1,
      companyId = 1,
      submittedBy = 1,
      management = 4,
      culture = 5,
      salary = 3,
      benefits = 0,
      wouldRecommend = 5,
      review = "Best place ever",
      created = Instant.now(),
      updated = Instant.now()
    )
  }

  private val _repo: ZIO.ServiceWithZIOPartiallyApplied[ReviewRepository] =
    ZIO.serviceWithZIO[ReviewRepository]

  private val _create = _repo(_.create(someReview))

  val tests: Spec[
    UserRepository
      with CompanyRepository
      with FlywayService
      with ReviewRepository,
    Object
  ] = suite("ReviewRepository")(
    test("create") {
      for {
        _ <- _create
      } yield assertCompletes
    },
    test("create fails for bad company fk") {
      for {
        err <- _repo(_.create(someReview.copy(companyId = 10))).flip
      } yield assertTrue(err.isInstanceOf[SQLException])
    },
    test("create fails for bad user fk") {
      for {
        err <- _repo(_.create(someReview.copy(submittedBy = 10))).flip
      } yield assertTrue(err.isInstanceOf[SQLException])
    },
    test("update") {
      for {
        rec     <- _create
        updated <- _repo(_.update(rec.id, rec => rec.copy(review = "Meh.")))
      } yield assertTrue(updated.review == "Meh.")
    },
    test("delete") {
      for {
        rec  <- _create
        _    <- _repo(_.delete(rec.id))
        gone <- _repo(_.getById(rec.id))
      } yield assertTrue(gone.isEmpty)
    },
    test("getById") {
      for {
        rec     <- _create
        fetched <- _repo(_.getById(rec.id))
      } yield assertTrue(fetched.contains(rec))
    },
    test("getReviewsByUser") {
      for {
        _    <- _create.repeatN(4) // 5 times total
        _    <- _repo(_.create(someReview.copy(submittedBy = 2))).repeatN(4)
        recs <- _repo(_.getByUserId(1))
      } yield assertTrue(
        recs.length == 5,
        recs.count(_.submittedBy == 1) == 5
      )
    },
    test("getReviewsByCompany") {
      for {
        _    <- _create.repeatN(4)
        _    <- _repo(_.create(someReview.copy(companyId = 2))).repeatN(4)
        recs <- _repo(_.getByCompanyId(1))
      } yield assertTrue(
        recs.length == 5,
        recs.count(_.companyId == 1) == 5
      )
    },
    test("cascade delete company fk") {
      for {
        _     <- _create.repeatN(4)
        recs  <- _repo(_.getByCompanyId(1))
        _     <- ZIO.serviceWithZIO[CompanyRepository](_.delete(1))
        recs2 <- _repo(_.getByCompanyId(1))
      } yield assertTrue(
        recs.length == 5,
        recs2.length == 0
      )
    },
    test("cascade delete user fk") {
      for {
        _     <- _create.repeatN(4)
        recs  <- _repo(_.getByCompanyId(1))
        _     <- ZIO.serviceWithZIO[UserRepository](_.delete(1))
        recs2 <- _repo(_.getByUserId(1))
      } yield assertTrue(
        recs.length == 5,
        recs2.length == 0
      )
    }
  ) @@ TestAspect.before {
    // To test foreign keys, and fetching by fk ids, we need extra data in the DB
    for {
      _ <- ZIO
             .serviceWithZIO[FlywayService](_.runMigrations)
      _ <- ZIO
             .serviceWithZIO[CompanyRepository](
               _.create(
                 CompanyRepositoryITSpec.genCompany.next()
               )
             )
             .repeatN(4)
      _ <- ZIO
             .serviceWithZIO[UserRepository](
               _.create(UserRepositoryITSpec.someUser)
             )
             .repeatN(4)
    } yield ()
  }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewRepositoryITSpec")(
      tests
    ).provideSome[Scope](
      Repository.quillPostgresLayer,
      UserRepositoryLive.layer,
      CompanyRepositoryLive.layer,
      ReviewRepositoryLive.layer,
      FlywayServiceLive.testContainerLayer,
      ZPostgreSQLContainer.Settings.default >>> ZPostgreSQLContainer.live
    )
}
