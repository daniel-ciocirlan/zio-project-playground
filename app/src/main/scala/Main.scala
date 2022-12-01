import clients.backend.BackEndClientLive
import com.raquo.airstream.core.EventStream
import com.raquo.laminar.api.L._
import domain.api.request.{LoginForm, RegisterAccountRequest}
import io.frontroute._
import layouts.Page
import org.scalajs.dom
import zio.Unsafe

import java.time.Instant
import scala.concurrent.Future

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

    val runtime = zio.Runtime.default
    val client  = BackEndClientLive.live

    // When we click a button, we will emit a Future that will fetch the current time
    val timeBus: EventBus[Future[Instant]] = new EventBus
    val processedTime                      = timeBus.events
      .flatMap(f =>
        EventStream.fromFuture(f)
      ) // Flatten out our value from the Future
      .map(_.toString())

    // quick hack - create user on load
    def fetchTimeInit(): Future[Instant] = {
      Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe.runToFuture(
          for {
            _    <- client
                      .createAccount(RegisterAccountRequest("me", "pass123"))
                      .ignore // silently fail if user already exists
            _    <- client.fetchToken(
                      LoginForm(username = "me", password = "pass123")
                    )
            time <- client.fetchTimeSecure()
          } yield time
        )
      }
    }

    def fetchTime(): Future[Instant] = {
      Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe.runToFuture(
          for {
            time <- client.fetchTimeSecure()
          } yield time
        )
      }
    }

    val timePage = Page(
      h1("The current time is"),
      p(
        child.text <-- processedTime,
        onMountCallback { _ =>
          // Seed the initial time
          timeBus.emit(fetchTimeInit())
        }
      ),
      button("refresh", onClick.mapTo(fetchTime()) --> timeBus)
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
