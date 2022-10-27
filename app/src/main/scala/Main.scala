import org.scalajs.dom
import com.raquo.laminar.api.L._

// Help from https://alvinalexander.com/scala/laminar-101-hello-world-example-static/

object Main {

  def main(args: Array[String]): Unit = {

    val rootElement: HtmlElement = div(
      h1("Hello, world")
    )

    // `#root` here must match the `id` in index.html
    val containerNode = dom.document.querySelector("#root")

    // this is how you render the rootElement in the browser
    render(containerNode, rootElement)

  }
}
