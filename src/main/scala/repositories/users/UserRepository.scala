package repositories.users

import domain.records.UserRecord
import io.getquill._
import io.getquill.jdbczio.Quill
import zio._

trait UserRepository {
  def create(record: UserRecord): Task[UserRecord]
  def update(id: Int, op: UserRecord => UserRecord): Task[UserRecord]
  def getById(id: Int): Task[Option[UserRecord]]
  def getByUserName(userName: String): Task[Option[UserRecord]]
  def delete(id: Int): Task[UserRecord]
}

case class UserRepositoryLive(quill: Quill.Postgres[SnakeCase])
    extends UserRepository {

  import quill._

  implicit val schema =
    schemaMeta[UserRecord]("users")

  implicit val userInsertMeta =
    insertMeta[UserRecord](_.id)

  implicit val userUpdateMeta =
    updateMeta[UserRecord](_.id)

  override def create(record: UserRecord): Task[UserRecord] =
    run(query[UserRecord].insertValue(lift(record)).returning(r => r))

  override def update(
      id: Int,
      op: UserRecord => UserRecord
  ): Task[UserRecord] = {
    for {
      current <- getById(id).someOrFail(
                   new Exception(s"Could not update due to missing id $id")
                 )
      updated <-
        run(
          query[UserRecord]
            .filter(_.id == lift(id))
            .updateValue(lift(op(current)))
            .returning(r => r)
        )
    } yield updated
  }

  override def getById(id: Int): Task[Option[UserRecord]] =
    run(query[UserRecord].filter(_.id == lift(id))).map(_.headOption)

  override def delete(id: Int): Task[UserRecord] =
    run(query[UserRecord].filter(_.id == lift(id)).delete.returning(r => r))

  override def getByUserName(username: String): Task[Option[UserRecord]] =
    run(query[UserRecord].filter(_.userName == lift(username))).map(_.headOption)
}

object UserRepositoryLive {

  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, UserRepositoryLive] =
    ZLayer {
      for {
        quill <- ZIO.service[Quill.Postgres[SnakeCase]]
      } yield UserRepositoryLive(quill)
    }

}
