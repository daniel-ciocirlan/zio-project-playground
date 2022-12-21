package pages

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import domain.api.request.{LoginForm, RegisterAccountRequest}
import domain.api.response.{TokenResponse, User}
import helpers.{Storage, ZJS}
import helpers.ZJS.ExtendedZIO
import io.frontroute.BrowserNavigation
import layouts.Page
import org.scalajs.dom.html
import org.scalajs.dom.html.Heading
import zio.ZIO

object SignUpPage {

  case class FormState(
      user: String = "",
      pass1: String = "",
      pass2: String = "",
      showErrors: Boolean = false,
      upstreamError: Option[String] = None
  ) {

    def userError: Option[String] =
      Option.when(user.isEmpty)("Username can't be empty")

    def passwordError: Option[String] =
      Option.when(pass1.isEmpty)("Password can't be empty")

    def passwordError2: Option[String] =
      Option.when(pass1 != pass2)("Passwords must match")

    def hasErrors: Boolean =
      userError.isDefined || passwordError.isDefined || passwordError2.isDefined || upstreamError.isDefined

    def errorMessage: Option[String] = {
      List(userError, passwordError, passwordError2, upstreamError)
        .find(_.isDefined)
        .flatten
    }
  }

  val stateVar: Var[FormState] = Var(FormState())

  val userWriter: Observer[String] =
    stateVar.updater[String]((state, user) =>
      state.copy(user = user, showErrors = false, upstreamError = None)
    )

  val pass1Writer: Observer[String] =
    stateVar.updater[String]((state, pass1) =>
      state.copy(pass1 = pass1, showErrors = false, upstreamError = None)
    )

  val pass2Writer: Observer[String] =
    stateVar.updater[String]((state, pass2) =>
      state.copy(pass2 = pass2, showErrors = false, upstreamError = None)
    )

  val submitter: Var[Option[User]] => Observer[FormState] =
    (userState: Var[Option[User]]) =>
      Observer[FormState] { state =>
        if (state.hasErrors) {
          stateVar.update(_.copy(showErrors = true))
        } else {
          stateVar.update(_.copy(showErrors = false))
          (for {
            _     <- ZJS
                       .client(
                         _.createAccount(
                           RegisterAccountRequest(
                             userName = state.user,
                             password = state.pass1
                           )
                         )
                       )
            token <-
              ZJS.client(
                _.fetchToken(
                  LoginForm(username = state.user, password = state.pass1)
                )
              )
          } yield {
            Storage.set("token", token)
            Storage.set("raw-token", token.accessToken)
            userState.set(Option(token.user))
            BrowserNavigation.replaceState("/")
          }).tapError { case e =>
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

  val signUpForm: L.Var[Option[User]] => ReactiveHtmlElement[html.Div] =
    (userState: Var[Option[User]]) =>
      div(
        className := "container my-lg-5 my-md-3 my-sm-1",
        h3(
          className := "pt-5",
          "Sign Up"
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
            .mapTo(stateVar.now()) --> submitter(userState),
          div(
            className := "form-group",
            label(forId        := "userid", "Username"),
            input(
              className        := "form-control",
              `type`           := "text",
              idAttr           := "userid",
              aria.describedBy := "useridHelp",
              placeholder      := "Enter a user id",
              controlled(
                value <-- stateVar.signal.map(_.user),
                onInput.mapToValue --> userWriter
              )
            ),
            small(
              idAttr           := "useridHelp",
              className        := "form-text form-muted",
              "This will identify you on this site"
            )
          ),
          div(
            className := "form-group",
            label(forId        := "password1", "Password"),
            input(
              className        := "form-control",
              `type`           := "password",
              idAttr           := "password1",
              aria.describedBy := "password1Help",
              placeholder      := "Enter a password",
              controlled(
                value <-- stateVar.signal.map(_.pass1),
                onInput.mapToValue --> pass1Writer
              )
            ),
            small(
              idAttr           := "password1Help",
              className        := "form-text form-muted",
              "It should be secure"
            )
          ),
          div(
            className := "form-group",
            label(forId        := "password2", "Password Again"),
            input(
              className        := "form-control",
              `type`           := "password",
              idAttr           := "password2",
              aria.describedBy := "password2Help",
              placeholder      := "Enter your password again",
              controlled(
                value <-- stateVar.signal.map(_.pass2),
                onInput.mapToValue --> pass2Writer
              )
            ),
            small(
              idAttr           := "password2Help",
              className        := "form-text form-muted",
              "It should be the same as above"
            )
          ),
          div(
            button(
              `type`    := "submit",
              className := "btn btn-primary",
              "Submit"
            ),
            a(
              className := "btn btn-outline-info m-2",
              href      := "/account/login",
              "Log In"
            )
          )
        )
      )

  val existingUser: User => ReactiveHtmlElement[Heading] = (user: User) =>
    h3(s"You're account already exists, ${user.userName}!")

  def apply(userState: Var[Option[User]]): HtmlElement = {
    Page(
      userState,
      userState.signal.now().map(existingUser).getOrElse(signUpForm(userState))
    )
  }

}
