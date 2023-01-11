package domain.api.response

import java.time.Instant
import upickle.default._

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

  implicit val instantRW: ReadWriter[Instant] = readwriter[String].bimap[Instant](
    f = i => i.toString,
    g = s => Instant.parse(s)
  )

  implicit val rw: ReadWriter[Review] = macroRW

}