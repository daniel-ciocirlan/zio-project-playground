package pages

import layouts.Page
import com.raquo.laminar.api.L._
import helpers.ZJS
import helpers.ZJS.ExtendedZIO
object CompanyPage {

  val content: Var[HtmlElement] = Var[HtmlElement](div())

  def buildContent(id: String) = {
    for {
      c       <- ZJS
                   .client(_.getCompanyById(id))
                   .someOrFail(new Exception("TODO Make this a 404"))
      reviews <- ZJS.client(_.getReviewsByCompanyId(c.id))
    } yield {
      div(
        className := "card",
        div(
          className := "card-body",
          h5(className := "card-title", c.name),
          p(
            className  := "card-text",
            "review summary will go here..."
          ),
          ul(
            className  := "list-group list-group-flush",
            // TODO if no reviews, suggest adding one
            reviews.map { r =>
              li(
                className := "list-group-item",
                r.review // TODO map this to a better component
              )
            }
          ),
          a(
            className  := "card-link",
            href       := c.url,
            "Website"
          ),
          a(
            className  := "card-link",
            href       := s"/reviews/add/${c.id}",
            "Add Review"
          )
        )
      )
    }
  }.map(s => content.set(s))

  def apply(companyId: String): HtmlElement = Page(
    div(
      onMountCallback(_ => buildContent(companyId).runJs),
      onUnmountCallback(_ => content.set(div())),
      className := "container my-lg-5 my-md-3 my-sm-1",
      child <-- content.signal
    )
  )
}
