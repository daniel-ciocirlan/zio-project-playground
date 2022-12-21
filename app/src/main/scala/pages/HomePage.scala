package pages

import layouts.Page
import com.raquo.laminar.api.L._
import domain.api.response.User

object HomePage {

  def apply(userState: Var[Option[User]]): HtmlElement = Page(
    userState,
    h1("Hello, world")
  )

}
