package pages

import com.raquo.laminar.api.L._
import layouts.Page

object AboutPage {

  def apply(): HtmlElement = Page(
    h1("About the site here")
  )

}
