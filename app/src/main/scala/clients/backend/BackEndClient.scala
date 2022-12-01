package clients.backend

import domain.api.request.{LoginForm, RegisterAccountRequest}
import domain.api.response.{TokenResponse, User}
import endpoints.{HealthEndpoints, UserEndpoints}
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

case class BackEndClientLive(
    backend: SttpBackend[Task, ZioStreams],
    config: BackEndClientConfig
) extends BackEndClient {

  val health = new HealthEndpoints {}
  val user   = new UserEndpoints {}

  val interpreter: SttpClientInterpreter = SttpClientInterpreter()

  override def createAccount(request: RegisterAccountRequest): Task[User] = {

    val _request: RegisterAccountRequest => Request[User, Any] =
      interpreter.toRequestThrowErrors(user.registerEndpoint, config.uri)

    backend.send(_request(request)).map(_.body)

  }

  override def fetchToken(request: LoginForm): Task[TokenResponse] = {

    val _request: LoginForm => Request[TokenResponse, Any] =
      interpreter.toRequestThrowErrors(user.generateTokenEndpoint, config.uri)

    backend.send(_request(request).header("X-FETCH-TOKEN", "true")).map(_.body)

  }

  override def fetchTime(): Task[Instant] = {

    val _request: Unit => Request[Instant, Any] =
      interpreter.toRequestThrowErrors(health.timeEndpoint, config.uri)

    backend.send(_request(())).map(_.body)

  }

  override def fetchTimeSecure(): Task[Instant] = {

    val _request: String => Unit => Request[Instant, Any] =
      interpreter.toSecureRequestThrowErrors(
        health.secureTimeEndpoint,
        config.uri
      )

    // We send an empty string here, because we're going to rely on a DelegateSttpBackend
    backend.send(_request("")(())).map(_.body)
  }

}

object BackEndClientLive {

  lazy val live: BackEndClientLive = BackEndClientLive(
    BearerBackend(FetchZioBackend()),
    BackEndClientConfig(
      uri = Some(uri"http://localhost:8080")
    )
  )

}
