package pages

import layouts.Page
import com.raquo.laminar.api.L._

object HomePage {

  def apply(): HtmlElement = Page(
    h1("Hello, world")
  )

}
