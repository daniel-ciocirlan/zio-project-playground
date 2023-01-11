package domain.api.request

import upickle.default._

case class CreateReviewRequest(
    companyId: Long,
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String
)

object CreateReviewRequest {
  implicit val rw: ReadWriter[CreateReviewRequest] = macroRW
}
