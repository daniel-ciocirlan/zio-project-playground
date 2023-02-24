package components
import com.raquo.laminar.api.L._

import java.util.UUID
object TextInput {

  def apply(
      title: String,
      stateWriter: Observer[String]
  ): HtmlElement = {
    val id = UUID.randomUUID().toString
    div(
      className := "form-group",
      label(
        forId     := id,
        title
      ),
      textArea(
        className := "form-control",
        idAttr    := id,
        onChange.mapToValue --> stateWriter,
        rows      := 10
      )
    )
  }

}
