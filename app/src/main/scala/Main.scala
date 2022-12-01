import clients.backend.BackEndClient
import com.raquo.airstream.core.EventStream
import com.raquo.laminar.api.L._
import domain.api.request.{LoginForm, RegisterAccountRequest}
import helpers.ZJS.{AppEnv, ExtendedZIO}
import io.frontroute._
import layouts.Page
import org.scalajs.dom
import zio.ZIO

import java.time.Instant

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

    val client: ZIO.ServiceWithZIOPartiallyApplied[AppEnv] =
      ZIO.serviceWithZIO[BackEndClient]

    val timeBus: EventBus[Instant]                            = new EventBus
    val zioTimeBus: EventBus[ZIO[AppEnv, Throwable, Instant]] = new EventBus
    val processedTime: EventStream[String]                    = EventStream
      .merge(
        timeBus.events,
        zioTimeBus.events.flatMap(z => EventStream.fromFuture(z.runJs))
      )
      .map(_.toString())

    // quick hack - create user on load
    def fetchTimeInit(): Unit = {
      val effect = for {
        _    <- client(
                  _.createAccount(RegisterAccountRequest("me", "pass123"))
                ).ignore // silently fail if user already exists
        _    <- client(
                  _.fetchToken(
                    LoginForm(username = "me", password = "pass123")
                  )
                )
        time <- client(_.fetchTimeSecure())
      } yield time
      effect.emitTo(timeBus)
    }

    val timePage = Page(
      h1("The current time is"),
      p(
        child.text <-- processedTime,
        onMountCallback { _ =>
          // Seed the initial time
          fetchTimeInit()
        }
      ),
      button(
        "refresh",
        onClick.mapTo(client(_.fetchTimeSecure())) --> zioTimeBus
      )
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
