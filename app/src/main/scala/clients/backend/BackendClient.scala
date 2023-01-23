package clients.backend

import domain.api.request.{
  CreateCompanyRequest,
  CreateReviewRequest,
  LoginForm,
  RegisterAccountRequest
}
import domain.api.response.{Company, Review, TokenResponse, User}
import endpoints.{
  CompanyEndpoints,
  HealthEndpoints,
  ReviewEndpoints,
  UserEndpoints
}
import state.AppState
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.client3.impl.zio.FetchZioBackend
import sttp.model.Uri
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio._

import java.time.Instant

trait BackEndClient {

  def createAccount(request: RegisterAccountRequest): Task[User]
  def fetchToken(request: LoginForm): Task[TokenResponse]
  def fetchTime(): Task[Instant]
  def fetchTimeSecure(): Task[Instant]
  def testUri(uri: Uri): Task[Boolean]

  def createCompany(request: CreateCompanyRequest): Task[Company]
  def getCompanies: Task[Seq[Company]]
  def getCompanyById(id: String): Task[Option[Company]]

  def createReview(request: CreateReviewRequest): Task[Review]
  def getReviewById(id: Long): Task[Option[Review]]
  def getReviewsByCompanyId(id: Long): Task[Seq[Review]]

}

case class BackEndClientConfig(uri: Option[Uri])

case class TokenExpiredError(msg: String) extends Exception(msg)

case class BackendClientLive(
    backend: SttpBackend[Task, ZioStreams],
    config: BackEndClientConfig
) extends BackEndClient {

  val health  = new HealthEndpoints {}
  val user    = new UserEndpoints {}
  val company = new CompanyEndpoints {}
  val review  = new ReviewEndpoints {}

  private val interpreter: SttpClientInterpreter = SttpClientInterpreter()
  private def endpointRequest[I, E, O](
      endpoint: Endpoint[Unit, I, E, O, Any]
  ): I => Request[Either[E, O], Any] =
    interpreter.toRequestThrowDecodeFailures(endpoint, config.uri)

  private def endpointRequestZIO[I, E <: Throwable, O](
      endpoint: Endpoint[Unit, I, E, O, Any]
  )(request: I): Task[O] =
    backend.send(endpointRequest(endpoint)(request)).map(_.body).absolve

  private def secureEndpointRequest[S, I, E, O](
      endpoint: Endpoint[S, I, E, O, Any]
  ): S => I => Request[Either[E, O], Any] =
    interpreter.toSecureRequestThrowDecodeFailures(endpoint, config.uri)

  private def secureEndpointRequestZIO[I, E <: Throwable, O](
      endpoint: Endpoint[String, I, E, O, Any]
  )(request: I): Task[O] =
    for {
      token    <- tokenOrFail
      response <- backend
                    .send(secureEndpointRequest(endpoint)(token)(request))
                    .map(_.body)
                    .absolve
    } yield response

  private def tokenOrFail: Task[String] = {
    for {
      now       <- Clock.instant
      userState <-
        ZIO
          .fromOption(AppState.userState.now())
          .orElseFail(
            TokenExpiredError("User credentials are missing. Please log in.")
          )
      _         <-
        ZIO
          .fail(
            TokenExpiredError(
              "User credentials have expired. Please log in again."
            )
          )
          .when(now.toEpochMilli > userState.expires)
    } yield userState.accessToken
  }

  override def createAccount(request: RegisterAccountRequest): Task[User] = {
    endpointRequestZIO(user.registerEndpoint)(request)
  }

  override def fetchToken(request: LoginForm): Task[TokenResponse] = {
    endpointRequestZIO(user.generateTokenEndpoint)(request)
  }

  override def fetchTime(): Task[Instant] = {
    endpointRequestZIO(health.timeEndpoint)(())
  }

  override def fetchTimeSecure(): Task[Instant] = {
    secureEndpointRequestZIO(health.secureTimeEndpoint)(())
  }

  override def testUri(uri: Uri): Task[Boolean] = {
    backend
      .send(
        basicRequest.get(uri)
      )
      .map(_.isSuccess)
  }

  override def createCompany(request: CreateCompanyRequest): Task[Company] = {
    secureEndpointRequestZIO(company.createEndpoint)(request)
  }

  override def getCompanies: Task[Seq[Company]] =
    endpointRequestZIO(company.getAllEndpoint)(())

  override def getCompanyById(id: String): Task[Option[Company]] =
    endpointRequestZIO(company.getByIdEndpoint)(id)

  override def createReview(request: CreateReviewRequest): Task[Review] =
    secureEndpointRequestZIO(review.createEndpoint)(request)

  override def getReviewById(id: Long): Task[Option[Review]] =
    endpointRequestZIO(review.getByIdEndpoint)(id)

  override def getReviewsByCompanyId(id: Long): Task[Seq[Review]] =
    endpointRequestZIO(review.getByCompanyIdEndpoint)(id)
}

object BackendClientLive {

  val layer: ZLayer[
    BackEndClientConfig with SttpBackend[Task, ZioStreams],
    Nothing,
    BackEndClient
  ] = ZLayer {
    for {
      sttp   <- ZIO.service[SttpBackend[Task, ZioStreams]]
      config <- ZIO.service[BackEndClientConfig]
    } yield BackendClientLive(sttp, config).asInstanceOf[BackEndClient]
  }

  val jsProvided: ZLayer[Any, Nothing, BackEndClient] =
    ZLayer.succeed(FetchZioBackend()) >+>
      ZLayer.succeed {
        BackEndClientConfig(
          uri = Some(uri"http://localhost:8080")
        )
      } >>>
      layer

  lazy val jsManual: BackendClientLive = BackendClientLive(
    FetchZioBackend(),
    BackEndClientConfig(
      uri = Some(uri"http://localhost:8080")
    )
  )

}
