package pages

import com.raquo.laminar.api.L._
import domain.api.request.LoginForm
import domain.api.response.User
import helpers.{Storage, ZJS}
import helpers.ZJS._
import io.frontroute.BrowserNavigation
import layouts.Page
import zio.ZIO

object LoginPage {

  case class FormState(
      user: String = "",
      password: String = "",
      showErrors: Boolean = false,
      upstreamError: Option[String] = None
  ) {
    def userError: Option[String] =
      Option.when(user.isEmpty)("User can't be empty")

    def passwordError: Option[String] =
      Option.when(password.isEmpty)("Password can't be empty")

    def maybeErrors: List[Option[String]] =
      List(userError, passwordError, upstreamError)

    def hasErrors: Boolean =
      maybeErrors.forall(_.isDefined)

    def errorMessage: Option[String] =
      maybeErrors.find(_.isDefined).flatten

  }

  val stateVar: Var[FormState] = Var(FormState())

  val userWriter: Observer[String] =
    stateVar.updater[String]((state, user) =>
      state.copy(user = user, showErrors = false, upstreamError = None)
    )

  val passwordWriter: Observer[String] =
    stateVar.updater[String]((state, pass) =>
      state.copy(password = pass, showErrors = false, upstreamError = None)
    )

  val submitter: Var[Option[User]] => Observer[FormState] = {
    (userState: Var[Option[User]]) =>
      Observer[FormState] { state =>
        if (state.hasErrors) {
          stateVar.update(_.copy(showErrors = true))
        } else {
          (
            for {
              token <-
                ZJS.client(_.fetchToken(LoginForm(state.user, state.password)))
            } yield {
              Storage.set("raw-token", token.accessToken)
              Storage.set("token", token)
              userState.set(Option(token.user))
              BrowserNavigation.replaceState("/")
            }
          ).tapError {case e =>
            ZIO.succeed(
              stateVar.update(
                _.copy(
                  showErrors = true,
                  upstreamError = Option(e.getMessage)
                )
              )
            )
          }.runJs
        }
      }
  }

  def apply(userState: Var[Option[User]]): HtmlElement = Page(
    userState,
    h1("Login goes here"),
    p("Or sign up:"),
    a(
      href := "/account/signup",
      "here"
    )
  )

}
