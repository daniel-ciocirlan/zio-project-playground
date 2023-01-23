package pages

import com.raquo.laminar.api.L._
import domain.api.request.CreateCompanyRequest
import helpers.ZJS
import helpers.ZJS.ExtendedZIO
import io.frontroute.BrowserNavigation
import layouts.Page
import sttp.client3.UriContext
import zio._

object CreateCompanyPage {

  case class FormState(
      name: String = "",
      website: String = "",
      showErrors: Boolean = false,
      upstreamError: Option[String] = None
  ) {

    private def nameError: Option[String] =
      Option.when(name.isBlank)("Company name can't be empty.")

    private def websiteError: Option[String] =
      Option.when(website.isBlank)("Company website can't be empty.")

    private val possibleErrors: List[Option[String]] = List(
      nameError,
      websiteError,
      upstreamError
    )

    def hasErrors: Boolean = possibleErrors.exists(_.isDefined)

    def errorMessage: Option[String] =
      possibleErrors.find(_.isDefined).flatten
  }

  val stateVar: Var[FormState] = Var(FormState())

  val nameWriter: Observer[String] =
    stateVar.updater[String]((state, name) =>
      state.copy(name = name, showErrors = false, upstreamError = None)
    )

  val urlWriter: Observer[String] =
    stateVar.updater[String]((state, url) =>
      state.copy(website = url, showErrors = false, upstreamError = None)
    )

  val submitter =
    Observer[FormState] { state =>
      if (state.hasErrors) {
        stateVar.update(_.copy(showErrors = true))
      } else {
        stateVar.update(_.copy(showErrors = false))
        (
          for {
            uri     <- ZIO.attempt(uri"${state.website}")
            _       <-
              ZJS
                .client(_.testUri(uri))
                .filterOrFail(_ == true)(
                  new Exception(
                    "The website you entered doesn't seem valid! Please check that it works."
                  )
                )
            company <-
              ZJS.client(
                _.createCompany(CreateCompanyRequest(state.name, state.website))
              )
          } yield {
            stateVar.set(FormState())
            BrowserNavigation.replaceState(s"/companies/${company.id}")
          }
        ).runJs
      }
    }

  def apply(): HtmlElement = Page(
  )

}
