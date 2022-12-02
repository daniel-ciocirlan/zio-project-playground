import com.raquo.laminar.api.L._
import io.frontroute._
import layouts.Page
import org.scalajs.dom
import pages.{
  AboutPage,
  CompaniesPage,
  HomePage,
  LoginPage,
  SignUpPage,
  TimePage
}

object Main {

  def main(args: Array[String]): Unit = {

    val routedSite = div(
      pathEnd {
        HomePage()
      },
      path("companies") {
        CompaniesPage()
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
