package domain.records

import domain.api.response.Company

case class CompanyRecord(
    id: Long,
    slug: String,
    name: String,
    url: String
)

object CompanyRecord {

  private def sanitizeSlug(name: String): String =
    name.toLowerCase.trim
      .replaceAll(" +", " ")
      .replaceAll(" ", "-")

  def apply(name: String, url: String): CompanyRecord = CompanyRecord(
    id = -1,
    slug = sanitizeSlug(name),
    name = name,
    url = url
  )

  implicit val conversion: CompanyRecord => Company =
    rec => Company(id = rec.id, slug = rec.slug, name = rec.name, url = rec.url)
}
