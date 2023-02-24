package components

import com.raquo.laminar.api.L._

import java.util.UUID

object NumberInput {

  def apply(
      title: String,
      stateWriter: Observer[Int]
  ): HtmlElement = {
    val id = UUID.randomUUID().toString
    div(
      className := "form-group",
      label(
        forId     := id,
        title
      ),
      input(
        className := "form-control",
        `type`    := "number",
        idAttr    := id,
        onChange.mapToValue.map(_.toInt) --> stateWriter
      )
    )
  }

}
