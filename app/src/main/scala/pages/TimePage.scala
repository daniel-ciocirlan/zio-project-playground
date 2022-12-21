package pages

import layouts.Page
import com.raquo.laminar.api.L._
import helpers.ZJS._

import java.time.Instant

object TimePage {

  def fetchTimeInit(): Unit = {
    val effect = for {
      time <- client(_.fetchTimeSecure())
    } yield time
    effect.emitTo(backendBus)
  }

  def apply(): HtmlElement = Page(
    h1("The current time is"),
    p(
      child.text <-- backendStream.keep[Instant].map(_.toString)
    ),
    button(
      "refresh",
      onClick.mapTo(client(_.fetchTimeSecure())) --> zioBackendBus
    )
  )
}
