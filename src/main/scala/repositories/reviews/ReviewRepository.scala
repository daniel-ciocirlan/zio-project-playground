package repositories.reviews

import domain.records.ReviewRecord
import io.getquill.{Ord, SnakeCase}
import io.getquill.jdbczio.Quill
import zio.{Task, ZIO, ZLayer}

trait ReviewRepository {

  def create(record: ReviewRecord): Task[ReviewRecord]
  def update(id: Int, txt: String): Task[ReviewRecord]
  def delete(id: Int): Task[ReviewRecord]
  def getById(id: Int): Task[Option[ReviewRecord]]
  def getReviewsByUser(userId: Int): Task[Seq[ReviewRecord]]
  def getReviewsByCompany(companyId: Int): Task[Seq[ReviewRecord]]

}

case class ReviewRepositoryLive(quill: Quill.Postgres[SnakeCase])
    extends ReviewRepository {

  import quill._

  implicit val schema  = schemaMeta[ReviewRecord]("reviews")
  implicit val insMeta = insertMeta[ReviewRecord](_.id)
  implicit val upMeta  = updateMeta[ReviewRecord](_.id)

  override def create(record: ReviewRecord): Task[ReviewRecord] =
    run(
      query[ReviewRecord]
        .insertValue(lift(record))
        .returning(r => r)
    )

  override def update(id: Int, txt: String): Task[ReviewRecord] =
    run(
      query[ReviewRecord]
        .filter(_.id == lift(id))
        .update(_.txt -> lift(txt))
        .returning(r => r)
    )

  override def delete(id: Int): Task[ReviewRecord] =
    run(
      query[ReviewRecord]
        .filter(_.id == lift(id))
        .delete
        .returning(r => r)
    )

  override def getById(id: Int): Task[Option[ReviewRecord]] =
    run(
      query[ReviewRecord]
        .filter(_.id == lift(id))
    ).map(_.headOption)

  override def getReviewsByUser(userId: Int): Task[Seq[ReviewRecord]] =
    run(
      query[ReviewRecord]
        .filter(_.userId == lift(userId))
        .sortBy(_.date)(Ord.desc)
    )

  override def getReviewsByCompany(companyId: Int): Task[Seq[ReviewRecord]] =
    run(
      query[ReviewRecord]
        .filter(_.companyId == lift(companyId))
        .sortBy(_.date)(Ord.desc)
    )
}

object ReviewRepositoryLive {

  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, ReviewRepository] =
    ZLayer {
      for {
        quill <- ZIO.service[Quill.Postgres[SnakeCase]]
      } yield ReviewRepositoryLive(quill)
    }

}
