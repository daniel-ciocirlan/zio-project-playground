package state

import com.raquo.laminar.api.L._
import domain.api.response.TokenResponse
import helpers.Storage

object AppState {

  val userState: Var[Option[TokenResponse]] =
    Var(Option.empty)

  def loadUserState: Unit = {
    userState.set(Storage.get[TokenResponse]("userState"))
  }

  def setUserState(tr: TokenResponse): Unit = {
    Storage.set("userState", tr)
    userState.set(Option(tr))
  }

  def clearUserState(): Unit = {
    Storage.clearAll()
    userState.set(Option.empty)
  }

}
