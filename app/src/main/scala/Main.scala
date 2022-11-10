import com.raquo.laminar.api.L._
import io.frontroute._
import layouts.Page
import org.scalajs.dom

object Main {

  def main(args: Array[String]): Unit = {

    val homePage = Page(
      h1("Hello, world")
    )

    val companiesPage = Page(
      h1("Company stuff here")
    )

    val aboutPage = Page(
      h1("About the site here")
    )

    val loginPage = Page(
      h1("Login goes here"),
      p("Stuff about registering too")
    )

    val routedSite = div(
      pathEnd {
        homePage
      },
      path("companies") {
        companiesPage
      },
      path("about") {
        aboutPage
      },
      pathPrefix("account") {
        path("login") {
          loginPage
        }
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

    // this is how you render the rootElement in the browser
    render(containerNode, routedSite.amend(LinkHandler.bind))

  }
}
