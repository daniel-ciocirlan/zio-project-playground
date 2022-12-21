package pages

import layouts.Page
import com.raquo.laminar.api.L._
import state.AppState

object LogoutPage {

  def apply() = Page(
    h3(
      "You have been successfully logged out.",
      onMountCallback(_ => {
        AppState.clearUserState()
      })
    )
  )
}
