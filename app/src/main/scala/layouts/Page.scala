package layouts

import org.scalajs.dom
import com.raquo.laminar.api.L._

trait Page {

}

object Page {

  def apply(elements: HtmlElement*): HtmlElement = {
    div(
      NavBar(),
      elements
    )
  }

}
