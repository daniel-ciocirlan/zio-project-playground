package services.flyway

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.flywaydb.core.Flyway
import services.config.ConfigService
import zio._

import scala.jdk.CollectionConverters._

trait FlywayService {
  def clean: Task[Unit]

  def runBaseline: Task[Unit]

  def runMigrations: Task[Unit]

  def repairMigrations: Task[Unit]
}

case class FlywayServiceLive(flyway: Flyway) extends FlywayService {

  override def clean: Task[Unit] = {
    for {
      result <- ZIO.attemptBlocking(flyway.clean())
      _      <-
        ZIO.logDebug(
          s"Schemas Cleaned:\n ${result.schemasCleaned.asScala.map(c => s"\t$c\n")}"
        )
      _      <-
        ZIO.logDebug(
          s"Schemas Dropped:\n ${result.schemasDropped.asScala.map(c => s"\t$c\n")}"
        )
      _      <-
        ZIO.logDebug(
          s"Clean Warnings:\n ${result.warnings.asScala.map(c => s"\t$c\n")}"
        )
    } yield ()
  }

  override def runBaseline: Task[Unit] =
    for {
      result <- ZIO.attemptBlocking(flyway.baseline())
      _      <-
        ZIO.logDebug(
          s"Baselined: ${result.successfullyBaselined}"
        )
      _      <-
        ZIO.logDebug(
          s"Baseline Warnings:\n ${result.warnings.asScala.map(c => s"\t$c\n")}"
        )
    } yield ()

  override def runMigrations: Task[Unit] =
    for {
      result <- ZIO.attemptBlocking(flyway.migrate())
      _      <-
        ZIO.logDebug(
          s"Migrations Ran:\n ${result.migrations.asScala
              .map(c => s"\t${c.filepath}\n")}"
        )
      _      <-
        ZIO.logDebug(
          s"Migration Warnings:\n ${result.warnings.asScala.map(c => s"\t$c\n")}"
        )
    } yield ()

  override def repairMigrations: Task[Unit] =
    for {
      result <- ZIO.attemptBlocking(flyway.repair())
      _      <-
        ZIO.logDebug(
          s"Repair Actions:\n ${result.repairActions.asScala
              .map(c => s"\t$c\n")}"
        )
      _      <-
        ZIO.logDebug(
          s"Repair Aligned:\n ${result.migrationsAligned.asScala
              .map(c => s"\t${c.filepath}\n")}"
        )
      _      <-
        ZIO.logDebug(
          s"Repair Removed:\n ${result.migrationsRemoved.asScala
              .map(c => s"\t${c.filepath}\n")}"
        )
      _      <-
        ZIO.logDebug(
          s"Repair Deleted:\n ${result.migrationsDeleted.asScala
              .map(c => s"\t${c.filepath}\n")}"
        )
      _      <-
        ZIO.logDebug(
          s"Repair Warnings:\n ${result.warnings.asScala.map(c => s"\t$c\n")}"
        )
    } yield ()
}

object FlywayServiceLive {

  val layer: ZLayer[FlywayConfig, Throwable, FlywayService] = ZLayer {
    for {
      config <- ZIO.service[FlywayConfig]
      flyway <- ZIO.attempt(
                  Flyway
                    .configure()
                    .dataSource(config.url, config.user, config.password)
                    .load()
                )
    } yield FlywayServiceLive(flyway)
  }

  val configuredLayer: ZLayer[Scope, Throwable, FlywayService] = {
    ConfigService.makeConfig[FlywayConfig](
      "rock.the.jvm.db.hikari-postgres.dataSource"
    ) >>> layer

  }

  val testContainerLayer
      : ZLayer[PostgreSQLContainer with Scope, Throwable, FlywayService] =
    ZLayer {
      for {
        container <- ZIO.service[PostgreSQLContainer]
      } yield services.flyway.FlywayConfig(
        url = container.jdbcUrl,
        user = container.username,
        password = container.password
      )
    } >>> layer
}
