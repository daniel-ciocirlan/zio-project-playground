package domain.api.request

import zio.json.{DeriveJsonCodec, JsonCodec}

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
  implicit val codec: JsonCodec[CreateReviewRequest] = DeriveJsonCodec.gen[CreateReviewRequest]
}
