package services.company

import domain.records.CompanyRecord
import repositories.companies.{
  CompanyRepository,
  CompanyRepositoryITSpec,
  MockCompanyRepository
}
import zio._
import zio.mock.Expectation
import zio.test._

object CompanyServiceITSpec extends ZIOSpecDefault {

  val service: ZIO.ServiceWithZIOPartiallyApplied[CompanyService] =
    ZIO.serviceWithZIO[CompanyService]

  val createLayer
      : ZLayer[Any, Nothing, CompanyRepository with CompanyRecord] = {
    val company = CompanyRepositoryITSpec.genCompany.next()
    MockCompanyRepository
      .Create(Assertion.anything, Expectation.value(company))
      .toLayer ++ ZLayer.succeed(company)
  }

  val getByIdLayer
      : ZLayer[Any, Nothing, CompanyRepository with CompanyRecord] = {
    val company = CompanyRepositoryITSpec.genCompany.next().copy(id = 1)
    (
      MockCompanyRepository.GetById(
        Assertion.equalTo(1L),
        Expectation.value(Option(company))
      ) and
        MockCompanyRepository.GetById(
          Assertion.anything,
          Expectation.value(Option.empty)
        )
    ).toLayer ++ ZLayer.succeed(company)
  }

  val getBySlugLayer
      : ZLayer[Any, Nothing, CompanyRepository with CompanyRecord] = {
    val company = CompanyRepositoryITSpec.genCompany.next()
    (
      MockCompanyRepository.GetBySlug(
        Assertion.equalTo(company.slug),
        Expectation.value(Option(company))
      ) and
        MockCompanyRepository.GetBySlug(
          Assertion.anything,
          Expectation.value(Option.empty)
        )
    ).toLayer ++ ZLayer.succeed(company)
  }

  val getAllLayer =
    MockCompanyRepository.Get(
      Expectation.value(CompanyRepositoryITSpec.genCompany.take(10).toSeq)
    )

  val tests = suite("CompanyService")(
    test("create") {
      for {
        record  <- ZIO.service[CompanyRecord]
        company <- service(_.create(record.name, record.url))
      } yield assertTrue(
        CompanyRecord.conversion(record) == company
      )
    }.provide(createLayer, CompanyServiceLive.layer),
    test("get by id") {
      for {
        record  <- ZIO.service[CompanyRecord]
        company <- service(_.getById(record.id))
        empty   <- service(_.getById(42))
      } yield assertTrue(
        company.contains(CompanyRecord.conversion(record)),
        empty.isEmpty
      )
    }.provide(getByIdLayer, CompanyServiceLive.layer),
    test("get by slug") {
      for {
        record  <- ZIO.service[CompanyRecord]
        company <- service(_.getBySlug(record.slug))
        empty   <- service(_.getBySlug("asdf"))
      } yield assertTrue(
        company.contains(CompanyRecord.conversion(record)),
        empty.isEmpty
      )
    }.provide(getBySlugLayer, CompanyServiceLive.layer),
    test("get all") {
      for {
        results <- service(_.getAll)
      } yield assertTrue(
        results.length == 10
      )
    }.provide(getAllLayer, CompanyServiceLive.layer)
  )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyServiceITSpec")(
      tests
    )
}
