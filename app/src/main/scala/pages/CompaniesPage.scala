package pages

import layouts.Page
import com.raquo.laminar.api.L._
import helpers.ZJS
import helpers.ZJS.ExtendedZIO

object CompaniesPage {

  var currentContent: Var[HtmlElement] = Var[HtmlElement](div())
  def buildTable                       =
    ZJS
      .client(_.getCompanies)
      .map { co =>
        table(
          className := "table",
          thead(
            tr(
              th("name"),
              th("website"),
              th("view")
            )
          ),
          tbody(
            co.map(c =>
              tr(
                td(c.name),
                td(a(c.url)),
                td(a("View", href := s"/companies/${c.id}"))
              )
            ): _*
          )
        )
      }
      .map(html => currentContent.set(html))

  def apply(): HtmlElement = Page(
    h1("Company stuff here"),
    a(
      className := "btn btn-outline-info m-2",
      href      := "/companies/create",
      "Add New Company"
    ),
    div(
      onMountCallback(_ => buildTable.runJs),
      child <-- currentContent.signal
    )
  )
}
