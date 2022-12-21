package pages

import helpers.Storage
import layouts.Page
import com.raquo.laminar.api.L._
import domain.api.response.User

object LogoutPage {

  def apply(userState: Var[Option[User]]) = Page(
    userState,
    h3(
      "You have been successfully logged out.",
      onMountCallback(_ => {
        Storage.clearAll()
        userState.set(Option.empty)
      })
    )
  )
}
