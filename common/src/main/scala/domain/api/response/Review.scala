package domain.api.response

import java.time.Instant
import zio.json.{DeriveJsonCodec, JsonCodec}

case class Review(
    id: Long,
    companyId: Long,
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: Instant,
    updated: Instant
)

object Review {
  implicit val codec: JsonCodec[Review] = DeriveJsonCodec.gen[Review]
}