package layouts

import org.scalajs.dom
import com.raquo.laminar.api.L._

object NavBar {

  def apply(): HtmlElement = {
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
            )
          ),
          ul(
            className := "navbar-nav d-flex",
            li(
              className := "nav-item",
              a(
                className := "nav-link",
                href      := "/account/login",
                "Log In"
              )
            )
          )
        )
      )
    )
  }
}
