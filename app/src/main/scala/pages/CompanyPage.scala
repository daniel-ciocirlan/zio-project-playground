package pages

import layouts.Page
import com.raquo.laminar.api.L._
import helpers.ZJS
import helpers.ZJS.ExtendedZIO
object CompanyPage {

  val content: Var[HtmlElement] = Var[HtmlElement](div())

  def buildContent(id: String) =
    ZJS
      .client(_.getCompanyById(id))
      .map {
        _.map { c =>
          div(
            className := "card",
            div(
              className := "card-body",
              h5(className := "card-title", c.name),
              p(
                className  := "card-text",
                "review summary will go here..."
              ),
              a(
                className  := "card-link",
                href       := c.url,
                "Website"
              )
            )
          )
        }.getOrElse(div())
      }
      .map(s => content.set(s))

  def apply(companyId: String): HtmlElement = Page(
    div(
      onMountCallback(_ => buildContent(companyId).runJs),
      onUnmountCallback(_ => content.set(div())),
      className := "container my-lg-5 my-md-3 my-sm-1",
      child <-- content.signal
    )
  )
}
