package controllers

import domain.api.request.{
  DeleteAccountRequest,
  RegisterRequest,
  UpdatePasswordRequest
}
import domain.api.response.User
import endpoints.UserEndpoints
import services.user.UserService
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio._

object UserController {
  def makeZIO: ZIO[UserService, Nothing, UserController] = for {
    service <- ZIO.service[UserService]
  } yield UserController(service)
}

case class UserController(userService: UserService)
    extends BaseController
    with UserEndpoints {

  val register: Full[Unit, Unit, RegisterRequest, Throwable, User, Any, Task] =
    registerEndpoint
      .serverLogic[Task](req =>
        userService.registerUser(req.userName, req.password).either
      )

  val updatePassword
      : Full[Unit, Unit, UpdatePasswordRequest, Throwable, User, Any, Task] =
    updatePasswordEndpoint
      .serverLogic[Task](req =>
        userService
          .updatePassword(req.userName, req.oldPassword, req.newPassword)
          .either
      )

  val delete
      : Full[Unit, Unit, DeleteAccountRequest, Throwable, User, Any, Task] =
    deleteEndpoint
      .serverLogic[Task](req =>
        userService.deleteAccount(req.userName, req.password).either
      )

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    register,
    updatePassword,
    delete
  )

}
