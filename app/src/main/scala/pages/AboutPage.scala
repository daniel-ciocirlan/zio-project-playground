package pages

import com.raquo.laminar.api.L._
import domain.api.response.User
import layouts.Page

object AboutPage {

  def apply(userState: Var[Option[User]]): HtmlElement = Page(
    userState,
    h1("About the site here")
  )

}
