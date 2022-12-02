package layouts

import com.raquo.laminar.api.L._

trait Page {}

object Page {

  def apply(elements: HtmlElement*): HtmlElement = {
    div(
      NavBar(),
      elements
    )
  }

  def apply(onMount: => Unit, elements: HtmlElement*): HtmlElement = {
    div(
      NavBar(),
      elements,
      onMountCallback(_ => onMount)
    )
  }

}
