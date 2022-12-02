package pages

import com.raquo.laminar.api.L._
import layouts.Page

object SignUpPage {

  case class FormState(
      user: String = "",
      pass1: String = "",
      pass2: String = "",
      showErrors: Boolean = false
  ) {

    def userError: Option[String] =
      Option.when(user.isEmpty)("Username can't be empty")

    def passwordError: Option[String] =
      Option.when(pass1.isEmpty)("Password can't be empty")

    def passwordError2: Option[String] =
      Option.when(pass1 != pass2)("Passwords must match")

    def hasErrors: Boolean =
      userError.isDefined || passwordError.isDefined || passwordError2.isDefined

    def displayError(error: FormState => Option[String]): Option[String] = {
      error(this).filter(_ => showErrors)
    }
  }

  val stateVar: Var[FormState] = Var(FormState())

  val userWriter: Observer[String] =
    stateVar.updater[String]((state, user) => state.copy(user = user))

  val pass1Writer: Observer[String] =
    stateVar.updater[String]((state, pass1) => state.copy(pass1 = pass1))

  val pass2Writer: Observer[String] =
    stateVar.updater[String]((state, pass2) => state.copy(pass2 = pass2))

  val submitter: Observer[FormState] = Observer[FormState] { state =>
    println(s"Submitting state $state")
    if (state.hasErrors) {
      println("There were errors...")
      // TODO need to update UI with message
      stateVar.update(_.copy(showErrors = true))
    } else {
      println("TODO Submit + redirect, etc...")
//      ZJS.client(_.createAccount(RegisterAccountRequest(userName = state.user, password = state.pass1)))
    }
  }

  def apply(): HtmlElement = Page(
    form(
      onSubmit.preventDefault
        .mapTo(stateVar.now()) --> submitter,
      div(
        className   := "form-group",
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
        className   := "form-group",
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
        className   := "form-group",
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
      button(`type` := "submit", className := "btn btn-primary", "Submit")
    )
  )

}
