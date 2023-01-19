package repositories.companies

import domain.records.CompanyRecord
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.{Task, ZIO, ZLayer}

trait CompanyRepository {
  def create(record: CompanyRecord): Task[CompanyRecord]
  def update(id: Long, op: CompanyRecord => CompanyRecord): Task[CompanyRecord]
  def delete(id: Long): Task[CompanyRecord]
  def getById(id: Long): Task[Option[CompanyRecord]]
  def getBySlug(slug: String): Task[Option[CompanyRecord]]
  def get: Task[Seq[CompanyRecord]]
}

case class CompanyRepositoryLive(quill: Quill.Postgres[SnakeCase])
    extends CompanyRepository {

  import quill._

  implicit val schema = schemaMeta[CompanyRecord]("companies")

  implicit val insMeta = insertMeta[CompanyRecord](_.id)

  implicit val upMeta = updateMeta[CompanyRecord](_.id)

  override def create(record: CompanyRecord): Task[CompanyRecord] =
    run(query[CompanyRecord].insertValue(lift(record)).returning(r => r))

  override def update(
      id: Long,
      op: CompanyRecord => CompanyRecord
  ): Task[CompanyRecord] = {
    for {
      current <- getById(id).someOrFail(
                   new Exception(s"Could not update due to missing id $id")
                 )
      updated <-
        run(
          query[CompanyRecord]
            .filter(_.id == lift(id))
            .updateValue(lift(op(current)))
            .returning(r => r)
        )
    } yield updated
  }

  override def delete(id: Long): Task[CompanyRecord] =
    run(query[CompanyRecord].filter(_.id == lift(id)).delete.returning(r => r))

  override def getById(id: Long): Task[Option[CompanyRecord]] =
    run(query[CompanyRecord].filter(_.id == lift(id))).map(_.headOption)

  override def getBySlug(slug: String): Task[Option[CompanyRecord]] =
    run(query[CompanyRecord].filter(_.slug == lift(slug))).map(_.headOption)

  override def get: Task[Seq[CompanyRecord]] =
    run(query[CompanyRecord])

}

object CompanyRepositoryLive {

  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, CompanyRepository] =
    ZLayer {
      for {
        quill <- ZIO.service[Quill.Postgres[SnakeCase]]
      } yield CompanyRepositoryLive(quill)
    }

}
