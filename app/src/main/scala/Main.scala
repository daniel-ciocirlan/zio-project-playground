import com.raquo.laminar.api.L._
import io.frontroute._
import layouts.Page
import org.scalajs.dom
import pages.{
  AboutPage,
  AddReviewPage,
  CompaniesPage,
  CompanyPage,
  CreateCompanyPage,
  HomePage,
  LoginPage,
  LogoutPage,
  SignUpPage,
  TimePage
}
import state.AppState

object Main {

  def main(args: Array[String]): Unit = {

    AppState.loadUserState

    val routedSite = div(
      pathEnd {
        HomePage()
      },
      pathPrefix("companies") {
        div(
          pathEnd {
            CompaniesPage()
          },
          path(segment) { path =>
            path match {
              case "create" => CreateCompanyPage()
              case id       => CompanyPage(id)
            }
          }
        )
      },
      pathPrefix("reviews") {
        div(
          pathPrefix("add") {
            path(segment) { companyId =>
              AddReviewPage(companyId.toLong) // TODO handle safely
            }
          }
        )
      },
      path("about") {
        AboutPage()
      },
      path("time") {
        TimePage()
      },
      pathPrefix("account") {
        div(
          path("login") {
            LoginPage()
          },
          path("signup") {
            SignUpPage()
          },
          path("logout") {
            LogoutPage()
          }
        )
      },
      (noneMatched & extractUnmatchedPath) { unmatched =>
        Page(
          h1("Not Found"),
          div(
            span("Not found path:"),
            span(unmatched.mkString("/", "/", ""))
          )
        )
      }
    )

    val containerNode = dom.document.querySelector("#app")

    render(containerNode, routedSite.amend(LinkHandler.bind))

  }
}
