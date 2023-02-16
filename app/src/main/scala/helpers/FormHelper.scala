package helpers

import com.raquo.laminar.api.L._

object FormHelper {

  def stateWriter[A, B](
      stateVar: Var[A],
      op: A => B => A
  ): Observer[B] = {
    stateVar.updater[B]((state, value) => op(state)(value))
  }

}
