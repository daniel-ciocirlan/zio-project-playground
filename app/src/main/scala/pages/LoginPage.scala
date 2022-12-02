package pages

import com.raquo.laminar.api.L._
import layouts.Page

object LoginPage {

  def apply(): HtmlElement = Page(
    h1("Login goes here"),
    p("Or sign up:"),
    a(
      href := "/account/signup",
      "here"
    )
  )

}
