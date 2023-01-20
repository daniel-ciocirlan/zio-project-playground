package services.review

import domain.api.request.CreateReviewRequest
import domain.api.response.Review
import domain.records.ReviewRecord
import repositories.reviews.ReviewRepository
import zio._

import java.time.Instant

trait ReviewService {
  def create(request: CreateReviewRequest, userId: Long): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(companyId: Long): Task[Seq[Review]]
}

case class ReviewServiceLive(
    reviewRepository: ReviewRepository
) extends ReviewService {
  override def create(
      request: CreateReviewRequest,
      userId: Long
  ): Task[Review] =
    reviewRepository
      .create(
        ReviewRecord(
          id = -1,
          companyId = request.companyId,
          submittedBy = userId,
          management = request.management,
          culture = request.culture,
          salary = request.salary,
          benefits = request.benefits,
          wouldRecommend = request.wouldRecommend,
          review = request.review,
          created = Instant.now(),
          updated = Instant.now()
        )
      )
      .map(ReviewRecord.conversion)

  override def getByCompanyId(companyId: Long): Task[Seq[Review]] =
    reviewRepository
      .getByCompanyId(companyId)
      .map(_.map(ReviewRecord.conversion))

  override def getById(id: Long): Task[Option[Review]] =
    reviewRepository
      .getById(id)
      .map(_.map(ReviewRecord.conversion))
}

object ReviewServiceLive {

  val layer: ZLayer[ReviewRepository, Nothing, ReviewService] = ZLayer {
    for {
      repo <- ZIO.service[ReviewRepository]
    } yield ReviewServiceLive(repo)
  }

}
