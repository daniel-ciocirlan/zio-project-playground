package pages

import layouts.Page
import com.raquo.laminar.api.L._
import domain.api.request.{LoginForm, RegisterAccountRequest}
import helpers.ZJS._

import java.time.Instant

object TimePage {

  // quick hack - create user on load
  def fetchTimeInit(): Unit = {
    val effect = for {
      _    <- client(
                _.createAccount(RegisterAccountRequest("me", "pass123"))
              ).ignore // silently fail if user already exists
      _    <- client(
                _.fetchToken(
                  LoginForm(username = "me", password = "pass123")
                )
              )
      time <- client(_.fetchTimeSecure())
    } yield time
    effect.emitTo(backendBus)
  }

  def apply(): HtmlElement = Page(
    onMount = fetchTimeInit(),
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
