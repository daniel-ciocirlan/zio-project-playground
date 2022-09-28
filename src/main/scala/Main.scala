import controllers.{BaseController, HealthController}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio._
import zhttp.service.Server
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  val port = 9000

  def makeControllers: ZIO[Any, Nothing, Seq[BaseController]] = for {
    health <- HealthController.makeZIO
  } yield Seq(health)

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

  val program: ZIO[Any, Throwable, ExitCode] = for {
    controllers <- makeControllers
    routes      <- gatherRoutes(controllers)
    _           <- ZIO.log(s"ZIO Project running at http://localhost:$port baby!")
    _           <- ZIO.log(s"Check the docs at http://localhost:$port/docs")
    _           <-
      Server.start(
        port,
        ZioHttpInterpreter().toHttp(routes)
      )
  } yield ExitCode.success

  override def run: ZIO[Any, Throwable, ExitCode] =
    program
      .provide(
        Runtime.removeDefaultLoggers >>> SLF4J.slf4j
      )
}
