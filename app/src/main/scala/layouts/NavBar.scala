package layouts

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import domain.api.response.User
import helpers.Storage
import org.scalajs.dom.html

object NavBar {

  val accountAction: User => ReactiveHtmlElement[html.Anchor] = (user: User) => a(
    className := "nav-link",
    href := "/account/logout",
    "Log Out"
  )

  def apply(userState: Var[Option[User]]): HtmlElement = {
    nav(
      className  := "navbar navbar-expand-lg bg-light",
      role       := "navigation",
      aria.label := "main navigation",
      div(
        className := "container-fluid",
        a(
          className     := "navbar-brand",
          href          := "/",
          "ZIO the JVM"
        ),
        button(
          className     := "navbar-toggler",
          `type`        := "button",
          aria.expanded := false,
          aria.controls := "navbarSupportedContent",
          aria.label    := "Toggle navigation",
          dataAttr("bs-toggle")("collapse"),
          dataAttr("bs-target")("#navbarSupportedContent"),
          span(
            className := "navbar-toggler-icon"
          )
        ),
        div(
          idAttr        := "navbarSupportedContent",
          className     := "collapse navbar-collapse",
          ul(
            className := "navbar-nav me-auto mb-2 mb-lg-0",
            li(
              className := "nav-item",
              a(
                className := "nav-link",
                href      := "/companies",
                "Companies"
              )
            ),
            li(
              className := "nav-item",
              a(
                className := "nav-link",
                href      := "/about",
                "About"
              )
            ),
            li(
              className := "nav-item",
              a(
                className := "nav-link",
                href      := "/time",
                "Current Time"
              )
            )
          ),
          ul(
            className := "navbar-nav d-flex",
            li(
              className := "nav-item",
              a(
                className := "nav-link",
                href      <-- userState.signal.map(opt => if (opt.isDefined) "/account/logout" else "/account/login"),
                child.text <-- userState.signal.map(opt => if (opt.isDefined) "Log Out" else "Log In")
              )
            )
          )
        )
      )
    )
  }
}
