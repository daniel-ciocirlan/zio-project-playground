package clients.backend

import domain.api.response.TokenResponse
import sttp.capabilities
import sttp.capabilities.zio.ZioStreams
import sttp.client3.{DelegateSttpBackend, Request, Response, SttpBackend}
import zio.{Task, ZIO, ZLayer}
import org.scalajs.dom

case class BearerBackend(
    delegate: SttpBackend[Task, ZioStreams]
) extends DelegateSttpBackend[Task, ZioStreams](delegate = delegate) {

  val tokenKey: String = "token"

  override def send[T, R >: ZioStreams with capabilities.Effect[Task]](
      request: Request[T, R]
  ): Task[Response[T]] = {

    val _newRequest = if (request.header("Authorization").isDefined) {
      request.auth.bearer(dom.window.localStorage.getItem(tokenKey))
    } else {
      request
    }

    delegate
      .send(_newRequest)
      .tap { response =>
        ZIO
          .attempt(
            dom.window.localStorage.setItem(
              tokenKey,
              response.body.asInstanceOf[TokenResponse].accessToken
            )
          )
          .when(request.header("X-FETCH-TOKEN").isDefined)
          .tapError(_ => ZIO.logError("Unable to store token automatically"))
          .ignore
      }

  }
}

object BearerBackend {

  val layer: ZLayer[SttpBackend[Task, ZioStreams], Nothing, SttpBackend[
    Task,
    ZioStreams
  ]] = ZLayer {
    for {
      delegate <- ZIO.service[SttpBackend[Task, ZioStreams]]
    } yield BearerBackend(delegate).asInstanceOf[SttpBackend[Task, ZioStreams]]
  }

}
