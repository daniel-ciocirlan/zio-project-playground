package components

import com.raquo.laminar.api.L._

import java.util.UUID

object FormSelect {

  def apply[O](
      title: String,
      options: Seq[O],
      selectedIndex: Int,
      stateWriter: Observer[O],
      modifiers: Modifier[Div]*
  )(implicit conversion: String => O): Div = {
    val id = UUID.randomUUID().toString
    div(
      className := "form-group",
      label(
        forId     := id,
        title
      ),
      select(
        className := "form-control",
        idAttr    := id,
        onChange.mapToValue.map(conversion) --> stateWriter,
        options.zipWithIndex.map { case (o, _) =>
          option(
            o.toString
          )
        }
      ),
      modifiers
    )
  }

}
