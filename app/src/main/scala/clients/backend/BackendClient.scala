package clients.backend

import domain.api.request.{LoginForm, RegisterAccountRequest}
import domain.api.response.{TokenResponse, User}
import endpoints.{HealthEndpoints, UserEndpoints}
import state.AppState
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.client3.impl.zio.FetchZioBackend
import sttp.model.Uri
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio._

import java.time.Instant

trait BackEndClient {

  def createAccount(request: RegisterAccountRequest): Task[User]
  def fetchToken(request: LoginForm): Task[TokenResponse]
  def fetchTime(): Task[Instant]
  def fetchTimeSecure(): Task[Instant]

}

case class BackEndClientConfig(uri: Option[Uri])

case class TokenExpiredError(msg: String) extends Exception(msg)

case class BackendClientLive(
    backend: SttpBackend[Task, ZioStreams],
    config: BackEndClientConfig
) extends BackEndClient {

  val health = new HealthEndpoints {}
  val user   = new UserEndpoints {}

  val interpreter: SttpClientInterpreter = SttpClientInterpreter()

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

    val _request
        : RegisterAccountRequest => Request[Either[Throwable, User], Any] =
      interpreter.toRequestThrowDecodeFailures(
        user.registerEndpoint,
        config.uri
      )

    backend
      .send(_request(request))
      .map(_.body)
      .absolve

  }

  override def fetchToken(request: LoginForm): Task[TokenResponse] = {

    val _request: LoginForm => Request[Either[Throwable, TokenResponse], Any] =
      interpreter.toRequestThrowDecodeFailures(
        user.generateTokenEndpoint,
        config.uri
      )

    backend
      .send(_request(request))
      .map(_.body)
      .absolve

  }

  override def fetchTime(): Task[Instant] = {

    val _request: Unit => Request[Either[Throwable, Instant], Any] =
      interpreter.toRequestThrowDecodeFailures(health.timeEndpoint, config.uri)

    backend
      .send(_request(()))
      .map(_.body)
      .absolve

  }

  override def fetchTimeSecure(): Task[Instant] = {

    val _request: String => Unit => Request[Instant, Any] =
      interpreter.toSecureRequestThrowErrors(
        health.secureTimeEndpoint,
        config.uri
      )

    for {
      token    <- tokenOrFail
      response <- backend.send(_request(token)(())).map(_.body)
    } yield response

  }

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
