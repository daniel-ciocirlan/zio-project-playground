package services.company

import domain.api.response.Company
import zio._
import domain.records.CompanyRecord
import repositories.companies.CompanyRepository

trait CompanyService {
  def create(name: String, url: String): Task[Company]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def getAll: Task[Seq[Company]]

}

case class CompanyServiceLive(companyRepository: CompanyRepository)
    extends CompanyService {
  override def create(name: String, url: String): Task[Company] =
    companyRepository
      .create(CompanyRecord(name, url))
      .map(CompanyRecord.conversion)

  override def getById(id: Long): Task[Option[Company]] =
    companyRepository
      .getById(id)
      .map(_.map(CompanyRecord.conversion))

  override def getAll: Task[Seq[Company]] =
    companyRepository.get
      .map(_.map(CompanyRecord.conversion))

  override def getBySlug(slug: String): Task[Option[Company]] =
    companyRepository
      .getBySlug(slug)
      .map(_.map(CompanyRecord.conversion))
}

object CompanyServiceLive {

  val layer: ZLayer[CompanyRepository, Nothing, CompanyService] = ZLayer {
    for {
      repo <- ZIO.service[CompanyRepository]
    } yield CompanyServiceLive(repo)
  }

}
