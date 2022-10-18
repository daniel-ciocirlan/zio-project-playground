package domain.records

import io.getquill.JsonValue

case class CompanyRecord(
    id: Int,
    slug: String,
    name: String,
    hq: Option[JsonValue[LocationRecord]],
    offices: JsonValue[Seq[LocationRecord]]
)
