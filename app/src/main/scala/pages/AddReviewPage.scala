package pages

import com.raquo.laminar.api.L._
import components.{FormSelect, NumberInput, TextInput}
import domain.api.request.CreateReviewRequest
import helpers.FormHelper.stateWriter
import helpers.ZJS
import helpers.ZJS._
import io.frontroute.BrowserNavigation
import layouts.Page
import zio.ZIO
object AddReviewPage {

  case class FormState(
      companyId: Long = -1,
      mgt: Int = 0,
      culture: Int = 0,
      salary: Int = 0,
      benefits: Int = 0,
      wouldRecommend: Int = 0,
      review: String = "",
      showErrors: Boolean = false,
      upstreamError: Option[String] = None
  ) {

    def hasErrors: Boolean = upstreamError.isDefined

    def clearErrors: FormState =
      this.copy(showErrors = false, upstreamError = None)

    def errorMessage: Option[String] =
      upstreamError

  }

  val stateVar: Var[FormState] = Var(FormState())

  val mtgWriter: Observer[Int] =
    stateWriter[FormState, Int](
      stateVar,
      fs => rating => fs.copy(mgt = rating).clearErrors
    )

  val cultureWriter: Observer[Int] =
    stateWriter[FormState, Int](
      stateVar,
      fs => rating => fs.copy(culture = rating).clearErrors
    )

  val salaryWriter: Observer[Int] =
    stateWriter[FormState, Int](
      stateVar,
      fs => rating => fs.copy(salary = rating).clearErrors
    )

  val benefitsWriter: Observer[Int] =
    stateWriter[FormState, Int](
      stateVar,
      fs => rating => fs.copy(benefits = rating).clearErrors
    )

  val recommendWriter: Observer[Int] =
    stateWriter[FormState, Int](
      stateVar,
      fs => rating => fs.copy(wouldRecommend = rating).clearErrors
    )

  val reviewWriter: Observer[String] = {
    stateWriter[FormState, String](
      stateVar,
      fs => review => fs.copy(review = review).clearErrors
    )

  }

  val submitter: Observer[FormState] =
    Observer[FormState] { state =>
      if (state.hasErrors) {
        stateVar.update(_.copy(showErrors = true))
      } else {
        stateVar.update(_.copy(showErrors = false))
        (
          for {
            _ <- ZJS.client(
                   _.createReview(
                     CreateReviewRequest(
                       companyId = state.companyId,
                       management = state.mgt,
                       culture = state.culture,
                       salary = state.salary,
                       benefits = state.benefits,
                       wouldRecommend = state.wouldRecommend,
                       review = state.review
                     )
                   )
                 )
          } yield {
            stateVar.set(FormState())
            BrowserNavigation.replaceState(s"/companies/${state.companyId}")
          }
        ).tapError { case e =>
          ZIO.succeed(
            stateVar.update(
              _.copy(showErrors = true, upstreamError = Option(e.getMessage))
            )
          )
        }.runJs
      }
    }

  val ratingRange: Seq[Int] =
    (0 to 5)

  val formContent = div(
    className := "container my-lg-5 my-md-3 my-sm-1",
    h3(
      className := "pt-5",
      "Add a company review"
    ),
    div(
      className := "alert alert-danger my-3",
      hidden <-- stateVar.signal.map(!_.showErrors),
      child.text <-- stateVar.signal.map(
        _.errorMessage.getOrElse("Something has gone wrong")
      )
    ),
    form(
      onSubmit.preventDefault
        .mapTo(stateVar.now()) --> submitter,
      FormSelect("Management", ratingRange, 0, mtgWriter)(
        _.toInt
      ),
      FormSelect("Culture", ratingRange, 0, cultureWriter)(
        _.toInt
      ),
      FormSelect("Benefits", ratingRange, 0, benefitsWriter)(
        _.toInt
      ),
      FormSelect(
        "Overall Recommendation",
        ratingRange,
        0,
        recommendWriter
      )(_.toInt),
      NumberInput("Salary (approximate)", salaryWriter),
      TextInput("Review", reviewWriter, className := "mb-2"),
      p(
        button(
          `type`    := "submit",
          className := "btn btn-primary",
          "Submit"
        )
      )
    )
  )

  def apply(companyId: Long): HtmlElement = Page(
    div(
      formContent,
      onMountCallback(_ => stateVar.set(FormState(companyId = companyId))),
      onUnmountCallback(_ => stateVar.set(FormState()))
    )
  )
}
