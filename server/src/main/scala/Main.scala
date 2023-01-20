import controllers.{
  BaseController,
  CompanyController,
  HealthController,
  ReviewController,
  UserController
}
import org.flywaydb.core.api.FlywayException
import repositories.Repository
import repositories.companies.CompanyRepositoryLive
import repositories.reviews.ReviewRepositoryLive
import repositories.users.UserRepositoryLive
import services.company.{CompanyService, CompanyServiceLive}
import services.flyway.{FlywayService, FlywayServiceLive}
import services.jwt.{JWTService, JWTServiceLive}
import services.review.{ReviewService, ReviewServiceLive}
import services.user.{UserService, UserServiceLive}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio._
import zio.http.Server
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  /** A method to build our BaseController implementations, and return them as a
    * Seq. All Controllers should be injected here. Since we should be building
    * our Controller implementations with the service module pattern, we don't
    * have to worry about our routes needing any dependencies, and they will
    * instead be surfaced here, and can be provided to the [[program]].
    *
    * @return
    */
  def makeControllers: ZIO[
    JWTService with CompanyService with UserService with ReviewService,
    Nothing,
    Seq[
      BaseController
    ]
  ] = for {
    health    <- HealthController.makeZIO
    users     <- UserController.makeZIO
    companies <- CompanyController.makeZIO
    reviews   <- ReviewController.makeZIO
  } yield Seq(health, users, companies, reviews)

  /** A method to aggregate the routes of our Controllers, and add swagger
    * documentation
    * @param controllers
    * @return
    */
  def gatherRoutes(
      controllers: Seq[BaseController]
  ): Task[List[ServerEndpoint[Any, Task]]] = ZIO.attempt {
    val combined = controllers.flatMap(_.routes).toList
    val doc      = SwaggerInterpreter().fromServerEndpoints(
      combined,
      "ZIO Project baby!",
      "1"
    )
    combined ++ doc
  }

  private def initMigrations: ZIO[FlywayService, Throwable, Unit] =
    for {
      flyway <- ZIO.service[FlywayService]
      _      <- flyway.runMigrations.catchSome { case _: FlywayException =>
                  flyway.repairMigrations *> flyway.runMigrations
                }
    } yield ()

  /** Our main server application
    */
  val program: ZIO[
    Server
      with UserService
      with CompanyService
      with ReviewService
      with JWTService
      with FlywayService,
    Throwable,
    ExitCode
  ] = for {
    _           <- initMigrations
    controllers <- makeControllers
    routes      <- gatherRoutes(controllers)
    _           <- ZIO.log(s"ZIO Project running at http://localhost:8080 baby!")
    _           <- ZIO.log(s"Check the docs at http://localhost:8080/docs")
    _           <- Server.serve(
                     ZioHttpInterpreter[Any](
                       ZioHttpServerOptions.default.appendInterceptor(
                         CORSInterceptor.default
                       )
                     ).toHttp(routes)
                   )
  } yield ExitCode.success

  override def run: ZIO[Any, Throwable, ExitCode] =
    program
      .provide(
        Scope.default,
        Server.default,
        Repository.dataSourceLayer,
        Repository.quillPostgresLayer,
        FlywayServiceLive.configuredLayer,
        UserRepositoryLive.layer,
        UserServiceLive.layer,
        CompanyRepositoryLive.layer,
        CompanyServiceLive.layer,
        JWTServiceLive.configuredLayer,
        ReviewRepositoryLive.layer,
        ReviewServiceLive.layer,
        Runtime.removeDefaultLoggers >>> SLF4J.slf4j // Make sure our ZIO.log's use slf4j
      )
}
