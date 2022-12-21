package layouts

import com.raquo.laminar.api.L._
import domain.api.response.User

trait Page {}

object Page {

  def apply(userState: Var[Option[User]], elements: HtmlElement*): HtmlElement = {
    div(
      NavBar(userState),
      elements
    )
  }

}
