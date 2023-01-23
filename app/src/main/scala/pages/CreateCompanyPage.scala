package pages

import com.raquo.laminar.api.L._
import domain.api.request.CreateCompanyRequest
import helpers.ZJS
import helpers.ZJS.ExtendedZIO
import io.frontroute.BrowserNavigation
import layouts.Page
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
            company <-
              ZJS.client(
                _.createCompany(CreateCompanyRequest(state.name, state.website))
              )
          } yield {
            stateVar.set(FormState())
            BrowserNavigation.replaceState(s"/companies/${company.id}")
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

  val createCompanyForm =
    div(
      className := "container my-lg-5 my-md-3 my-sm-1",
      h3(
        className := "pt-5",
        "Create a Company Listing"
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
        div(
          className := "form-group",
          label(forId        := "company", "Company Name"),
          input(
            className        := "form-control",
            `type`           := "text",
            idAttr           := "company",
            aria.describedBy := "companyHelp",
            placeholder      := "Enter the Company Name",
            controlled(
              value <-- stateVar.signal.map(_.name),
              onInput.mapToValue --> nameWriter
            )
          ),
          small(
            idAttr           := "companyHelp",
            className        := "form-text form-muted",
            "The company name that will show on the site"
          )
        ),
        div(
          className := "form-group",
          label(forId        := "url", "Company Website"),
          input(
            className        := "form-control",
            `type`           := "text",
            idAttr           := "url",
            aria.describedBy := "urlHelp",
            placeholder      := "Enter the Company Website",
            controlled(
              value <-- stateVar.signal.map(_.website),
              onInput.mapToValue --> urlWriter
            )
          ),
          small(
            idAttr           := "urlHelp",
            className        := "form-text form-muted",
            "The company's website"
          )
        ),
        button(
          `type`    := "submit",
          className := "btn btn-primary",
          "Submit"
        )
      )
    )

  def apply(): HtmlElement = Page(
    createCompanyForm
  )

}
