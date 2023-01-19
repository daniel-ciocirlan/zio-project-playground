package controllers

import domain.api.request.CreateCompanyRequest
import domain.api.response.Company
import endpoints.CompanyEndpoints
import services.company.CompanyService
import services.jwt.{JWTService, ValidatedUserToken}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio.{Task, ZIO}

object CompanyController {
  def makeZIO: ZIO[JWTService with CompanyService, Nothing, CompanyController] =
    for {
      companyService <- ZIO.service[CompanyService]
      jwtService     <- ZIO.service[JWTService]
    } yield CompanyController(companyService, jwtService)
}

case class CompanyController(
    companyService: CompanyService,
    jwtService: JWTService
) extends BaseController
    with CompanyEndpoints {

  val create: Full[
    String,
    ValidatedUserToken,
    CreateCompanyRequest,
    Throwable,
    Company,
    Any,
    Task
  ] =
    createEndpoint
      .serverSecurityLogic[ValidatedUserToken, Task](token =>
        jwtService.verifyToken(token).either
      )
      .serverLogic(_ => req => companyService.create(req.name, req.url).either)

  val get: Full[Unit, Unit, Unit, Throwable, Seq[Company], Any, Task] =
    getAllEndpoint
      .serverLogic[Task](_ => companyService.getAll.either)

  val getById =
    getByIdEndpoint
      .serverLogic[Task] { strId =>
        ZIO
          .attempt(strId.toLong)
          .flatMap(longId => companyService.getById(longId))
          .catchSome { case _: NumberFormatException =>
            companyService.getBySlug(strId)
          }
          .either
      }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    create,
    get,
    getById
  )
}
