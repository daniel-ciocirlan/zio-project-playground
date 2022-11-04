package domain.records

import java.time.Instant

case class ReviewRecord(
    id: Int,
    companyId: Int,
    userId: Int,
    txt: String,
    date: Instant
)
