package services.review

import domain.api.request.CreateReviewRequest
import domain.records.ReviewRecord
import repositories.reviews.{
  MockReviewRepository,
  ReviewRepository,
  ReviewRepositoryITSpec
}
import zio._
import zio.mock.Expectation
import zio.test._

object ReviewServiceITSpec extends ZIOSpecDefault {

  val reviewRecord = ReviewRepositoryITSpec.someReview

  val service: ZIO.ServiceWithZIOPartiallyApplied[ReviewService] =
    ZIO.serviceWithZIO[ReviewService]

  val createLayer: ULayer[ReviewRepository] =
    MockReviewRepository
      .Create(Assertion.anything, Expectation.value(reviewRecord))
      .toLayer

  val getByIdLayer: ULayer[ReviewRepository] =
    (MockReviewRepository
      .GetById(
        Assertion.equalTo(1L),
        Expectation.value(Some(reviewRecord.copy(id = 1)))
      ) and
      MockReviewRepository
        .GetById(Assertion.anything, Expectation.value(Option.empty))).toLayer

  val getByCompanyIdLayer: ULayer[ReviewRepository] =
    (MockReviewRepository
      .GetByCompanyId(
        Assertion.equalTo(1L),
        Expectation.value(
          Seq(reviewRecord, reviewRecord, reviewRecord).zipWithIndex
            .map(t => t._1.copy(id = t._2 + 1))
        )
      ) and
      MockReviewRepository
        .GetByCompanyId(
          Assertion.anything,
          Expectation.value(Seq.empty)
        )).toLayer

  val tests = suite("ReviewService")(
    test("create") {
      for {
        inserted <- service(
                      _.create(
                        CreateReviewRequest(
                          companyId = -1,
                          management = 1,
                          culture = 2,
                          salary = 3,
                          benefits = 4,
                          wouldRecommend = 5,
                          review = "great"
                        ),
                        1
                      )
                    )
      } yield assertTrue(
        inserted == ReviewRecord.conversion(reviewRecord)
      )
    }.provide(createLayer, ReviewServiceLive.layer),
    test("get by id") {
      for {
        exists <- service(_.getById(1))
        empty  <- service(_.getById(2))
      } yield assertTrue(
        exists.isDefined,
        empty.isEmpty
      )
    }.provide(getByIdLayer, ReviewServiceLive.layer),
    test("get by company id") {
      for {
        empty   <- service(_.getByCompanyId(2))
        results <- service(_.getByCompanyId(1))
      } yield assertTrue(
        empty.isEmpty,
        results.size == 3
      )
    }.provide(getByCompanyIdLayer, ReviewServiceLive.layer)
  )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewServiceITSpec")(
      tests
    )
}
