package domain.records

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
