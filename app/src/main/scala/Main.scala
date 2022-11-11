import com.raquo.airstream.core.EventStream
import com.raquo.laminar.api.L._
import endpoints.HealthEndpoints
import io.frontroute._
import layouts.Page
import org.scalajs.dom
import sttp.client3.{FetchBackend, Request, UriContext}
import sttp.tapir.client.sttp.SttpClientInterpreter

import java.time.Instant
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

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

    val backend         = FetchBackend()
    val healthEndpoints = new HealthEndpoints {}

    // When we click a button, we will emit a Future that will fetch the current time
    val timeBus: EventBus[Future[Instant]] = new EventBus
    val processedTime                      = timeBus.events
      .flatMap(f =>
        EventStream.fromFuture(f)
      ) // Flatten out our value from the Future
      .map(_.toString())

    val timeRequest: Unit => Request[Instant, Any] = {
      SttpClientInterpreter().toRequestThrowErrors(
        healthEndpoints.timeEndpoint,
        Some(uri"http://localhost:9000")
      )
    }

    /// Working on this part... needs to fix CORS
    def fetchTime(): Future[Instant] =
      backend.send(timeRequest(())).map(_.body)

    def fakeIt(): Future[Instant] = {
      Future(Instant.now())
    }

    val timePage = Page(
      h1("The current time is"),
      p(
        child.text <-- processedTime,
        onMountCallback { _ =>
          // Seed the initial time
          timeBus.emit(fakeIt())
        }
      ),
      button("refresh", onClick.mapTo(fakeIt()) --> timeBus)
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
      path("time") {
        timePage
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
