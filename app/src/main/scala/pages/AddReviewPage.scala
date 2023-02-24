package pages

import com.raquo.laminar.api.L._
import components.{FormSelect, NumberInput, TextInput}
import helpers.FormHelper.stateWriter
import layouts.Page
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

    def errorMessage: Option[String] =
      Option.empty

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

//  val submitter: Observer[FormState] = ???

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
      FormSelect("Management", ratingRange, stateWriter = mtgWriter)(_.toInt),
      FormSelect("Culture", ratingRange, stateWriter = cultureWriter)(_.toInt),
      FormSelect("Benefits", ratingRange, stateWriter = benefitsWriter)(
        _.toInt
      ),
      FormSelect(
        "Overall Recommendation",
        ratingRange,
        stateWriter = recommendWriter
      )(_.toInt),
      NumberInput("Salary (approximate)", salaryWriter),
      TextInput("Review", reviewWriter)
    )
  )

  def apply(companyId: Long): HtmlElement = Page(
    formContent
  )
}
