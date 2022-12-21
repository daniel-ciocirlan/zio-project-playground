package pages

import com.raquo.laminar.api.L._
import domain.api.request.LoginForm
import helpers.ZJS
import helpers.ZJS._
import io.frontroute.BrowserNavigation
import layouts.Page
import state.AppState
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

  val submitter: Observer[FormState] = {
    Observer[FormState] { state =>
      if (state.hasErrors) {
        stateVar.update(_.copy(showErrors = true))
      } else {
        (
          for {
            token <-
              ZJS.client(_.fetchToken(LoginForm(state.user, state.password)))
          } yield {
            AppState.setUserState(token)
            stateVar.set(FormState())
            BrowserNavigation.replaceState("/")
          }
        ).tapError { case e =>
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

  def apply(): HtmlElement = Page(
    div(
      className := "container my-lg-5 my-md-3 my-sm-1",
      h3(
        className := "pt-5",
        "Log In"
      ),
      div(
        className := "alert alert-danger my-3",
        hidden <-- stateVar.signal.map(!_.showErrors),
        child.text <-- stateVar.signal.map(
          _.errorMessage.getOrElse("Something has gone wrong")
        )
      ),
      form(
        onSubmit.preventDefault
          .mapTo(stateVar.now()) --> submitter,
        div(
          className := "form-group",
          label(forId        := "userid", "Username"),
          input(
            className        := "form-control",
            `type`           := "text",
            idAttr           := "userid",
            aria.describedBy := "useridHelp",
            placeholder      := "Your user id",
            controlled(
              value <-- stateVar.signal.map(_.user),
              onInput.mapToValue --> userWriter
            )
          )
        ),
        div(
          className := "form-group",
          label(forId        := "password", "Password"),
          input(
            className        := "form-control",
            `type`           := "password",
            idAttr           := "password",
            aria.describedBy := "password1Help",
            placeholder      := "Your password",
            controlled(
              value <-- stateVar.signal.map(_.password),
              onInput.mapToValue --> passwordWriter
            )
          )
        ),
        div(
          button(
            `type`    := "submit",
            className := "btn btn-primary m-2",
            "Submit"
          ),
          a(
            className := "btn btn-outline-info m-2",
            href      := "/account/signup",
            "Register"
          )
        )
      )
    )
  )

}
