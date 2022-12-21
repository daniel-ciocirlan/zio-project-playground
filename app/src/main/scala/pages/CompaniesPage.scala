package pages

import layouts.Page
import com.raquo.laminar.api.L._

object CompaniesPage {

  def apply(): HtmlElement = Page(
    h1("Company stuff here")
  )
}
