package domain.records

import domain.api.response.Company

case class CompanyRecord(
    id: Long,
    slug: String,
    name: String,
    url: String
)

object CompanyRecord {
  implicit val conversion: CompanyRecord => Company =
    rec => Company(id = rec.id, slug = rec.slug, name = rec.name, url = rec.url)
}
