package domain.records

import domain.api.response.Review

import java.time.Instant

case class ReviewRecord(
    id: Long,
    companyId: Long,
    submittedBy: Long,
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: Instant,
    updated: Instant
)

object ReviewRecord {
  implicit val conversion: ReviewRecord => Review =
    rec =>
      Review(
        id = rec.id,
        companyId = rec.companyId,
        management = rec.management,
        culture = rec.culture,
        salary = rec.salary,
        benefits = rec.benefits,
        wouldRecommend = rec.wouldRecommend,
        review = rec.review,
        created = rec.created,
        updated = rec.updated
      )
}
