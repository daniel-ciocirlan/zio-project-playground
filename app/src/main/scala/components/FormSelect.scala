package components

import com.raquo.laminar.api.L._

import java.util.UUID

case class FormSelect[O](label: String, options: Seq[O]) {
  div(
    className := "form-group"
  )
}

object FormSelect {

  def apply(title: String, options: Seq[String]) = {
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
        options.map(o => option(o))
      )
    )
  }

}
