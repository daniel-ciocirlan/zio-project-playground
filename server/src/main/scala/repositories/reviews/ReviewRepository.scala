package repositories.reviews

import domain.records.ReviewRecord
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio._

trait ReviewRepository {
  def create(record: ReviewRecord): Task[ReviewRecord]
  def update(id: Long, op: ReviewRecord => ReviewRecord): Task[ReviewRecord]
  def delete(id: Long): Task[ReviewRecord]
  def getById(id: Long): Task[Option[ReviewRecord]]
  def getByCompanyId(companyId: Long): Task[Seq[ReviewRecord]]
  def getByUserId(userId: Long): Task[Seq[ReviewRecord]]
}

case class ReviewRepositoryLive(quill: Quill.Postgres[SnakeCase])
    extends ReviewRepository {

  import quill._

  implicit val schema  = schemaMeta[ReviewRecord]("reviews")
  implicit val insMeta = insertMeta[ReviewRecord](_.id, _.created, _.updated)
  implicit val upMeta  =
    updateMeta[ReviewRecord](_.id, _.created, _.companyId, _.submittedBy)

  override def create(record: ReviewRecord): Task[ReviewRecord] =
    run(query[ReviewRecord].insertValue(lift(record)).returning(r => r))

  override def update(
      id: Long,
      op: ReviewRecord => ReviewRecord
  ): Task[ReviewRecord] = {
    for {
      current <- getById(id).someOrFail(
                   new Exception(s"Could not update due to missing id $id")
                 )
      updated <-
        run(
          query[ReviewRecord]
            .filter(_.id == lift(id))
            .updateValue(lift(op(current)))
            .returning(r => r)
        )
    } yield updated
  }

  override def delete(id: Long): Task[ReviewRecord] =
    run(query[ReviewRecord].filter(_.id == lift(id)).delete.returning(r => r))

  override def getById(id: Long): Task[Option[ReviewRecord]] =
    run(query[ReviewRecord].filter(_.id == lift(id))).map(_.headOption)

  override def getByCompanyId(companyId: Long): Task[Seq[ReviewRecord]] =
    run(query[ReviewRecord].filter(_.companyId == lift(companyId)))

  override def getByUserId(userId: Long): Task[Seq[ReviewRecord]] =
    run(query[ReviewRecord].filter(_.submittedBy == lift(userId)))
}

object ReviewRepositoryLive {

  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, ReviewRepository] =
    ZLayer {
      for {
        quill <- ZIO.service[Quill.Postgres[SnakeCase]]
      } yield ReviewRepositoryLive(quill)
    }

}
