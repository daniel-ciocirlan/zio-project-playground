package pages

import com.raquo.laminar.api.L._
import domain.api.response.Review
import helpers.ZJS
import helpers.ZJS.{AppEnv, ExtendedZIO}
import layouts.Page
import zio.ZIO

import scala.math.BigDecimal.RoundingMode
object CompanyPage {

  val content: Var[HtmlElement] = Var[HtmlElement](div())

  private def ratingHelper(ratings: Seq[Int]): String = {
    BigDecimal
      .decimal(ratings.sum.toDouble / ratings.size.toDouble)
      .setScale(1, RoundingMode.UP)
      .toString() + "/5"
  }

  private def ratingHelper(rating: Int): String = ratingHelper(Seq(rating))

  private def overviewBuilder(reviews: Seq[Review]): Div = {
    if (reviews.isEmpty) {
      div(
        p(
          "No reviews yet!"
        )
      )
    } else {
      div(
        p(
          className := "card-text",
          s"Overall recommendation: ${ratingHelper(reviews.map(_.wouldRecommend))}"
        ),
        p(
          className := "card-text",
          s"Management: ${ratingHelper(reviews.map(_.management))}"
        ),
        p(
          className := "card-text",
          s"Culture: ${ratingHelper(reviews.map(_.culture))}"
        ),
        p(
          className := "card-text",
          s"Benefits: ${ratingHelper(reviews.map(_.benefits))}"
        )
      )
    }

  }
  private def reviewBuilder(reviews: Seq[Review]): Div = {
    if (reviews.isEmpty) {
      div()
    } else {
      div(
        h5(
          "All Reviews:"
        ),
        reviews.map { r =>
          div(
            className := "card mb-2",
            div(
              className := "card-body",
              p(
                className := "card-text",
                s"Would recommend: ${ratingHelper(r.wouldRecommend)}"
              ),
              p(
                className := "card-text",
                s"Management: ${ratingHelper(r.management)}"
              ),
              p(
                className := "card-text",
                s"Culture: ${ratingHelper(r.culture)}"
              ),
              p(
                className := "card-text",
                s"Benefits: ${ratingHelper(r.benefits)}"
              ),
              p(
                className := "card-text",
                s"Salary: $$${r.salary / 1000}k"
              ),
              h5("Review"),
              p(
                className := "card-text",
                r.review
              )
            )
          )
        }
      )

    }
  }

  def buildContent(id: String): ZIO[AppEnv, Throwable, Unit] = {
    for {
      c       <- ZJS
                   .client(_.getCompanyById(id))
                   .someOrFail(new Exception("TODO Make this a 404"))
      reviews <- ZJS.client(_.getReviewsByCompanyId(c.id))
    } yield {
      div(
        div(
          className := "card mb-2",
          div(
            className := "card-body",
            h5(className := "card-title", c.name),
            overviewBuilder(reviews),
            a(
              className  := "card-link",
              href       := c.url,
              "Company Website"
            ),
            a(
              className  := "card-link",
              href       := s"/reviews/add/${c.id}",
              "Add Review"
            )
          )
        ),
        reviewBuilder(reviews)
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
