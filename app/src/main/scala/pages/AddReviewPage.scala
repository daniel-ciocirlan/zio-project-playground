package pages

import com.raquo.laminar.api.L._
import components.FormSelect
import helpers.ZJS
import helpers.ZJS.ExtendedZIO
import io.frontroute.BrowserNavigation
import helpers.FormHelper.stateWriter
import layouts.Page
import zio._
object AddReviewPage {

  case class FormState(
      mgt: Int = 0,
      culture: Int = 0,
      salary: Int = 0,
      benefits: Int = 0,
      wouldRecommend: Int = 0,
      review: String = "",
      showErrors: Boolean = false,
      upstreamError: Option[String] = None
  ) {
    def clearErrors: FormState =
      this.copy(showErrors = false, upstreamError = None)
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

  val reviewWriter: Observer[String] =
    stateVar.updater[String]((state, value) =>
      state.copy(review = value).clearErrors
    )

  val ratingRange: Seq[String]            =
    (0 to 5).map(_.toString)
  def apply(companyId: Long): HtmlElement = Page(
    FormSelect("Stuff", ratingRange)
  )
}
