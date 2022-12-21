import com.raquo.laminar.api.L._
import domain.api.response.{TokenResponse, User}
import helpers.Storage
import io.frontroute._
import layouts.Page
import org.scalajs.dom
import pages.{AboutPage, CompaniesPage, HomePage, LoginPage, LogoutPage, SignUpPage, TimePage}

object Main {

  def main(args: Array[String]): Unit = {

    // TODO this would likely be more manageable as part of a "global app state"
    val userState: Var[Option[User]] =
      Var(Storage.get[TokenResponse]("token").map(_.user))

    val routedSite = div(
      pathEnd {
        HomePage(userState)
      },
      path("companies") {
        CompaniesPage(userState)
      },
      path("about") {
        AboutPage(userState)
      },
      path("time") {
        TimePage(userState)
      },
      pathPrefix("account") {
        div(
          path("login") {
            LoginPage(userState)
          },
          path("signup") {
            SignUpPage(userState)
          },
          path("logout") {
            LogoutPage(userState)
          }
        )
      },
      (noneMatched & extractUnmatchedPath) { unmatched =>
        Page(
          userState,
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
