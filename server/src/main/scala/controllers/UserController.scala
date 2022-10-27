package controllers

import domain.api.request.{
  DeleteAccountRequest,
  RegisterRequest,
  UpdatePasswordRequest
}
import domain.api.response.User
import services.user.UserService
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio._

object UserController {
  def makeZIO: ZIO[UserService, Nothing, UserController] = for {
    service <- ZIO.service[UserService]
  } yield UserController(service)
}

case class UserController(userService: UserService) extends BaseController {

  val register: Full[Unit, Unit, RegisterRequest, Throwable, User, Any, Task] =
    baseEndpoint
      .tag("users")
      .name("register")
      .description("Register a user account with username and password")
      .in("users")
      .post
      .in(jsonBody[RegisterRequest])
      .out(jsonBody[User])
      .serverLogic[Task](req =>
        userService.registerUser(req.userName, req.password).either
      )

  val updatePassword
      : Full[Unit, Unit, UpdatePasswordRequest, Throwable, User, Any, Task] =
    baseEndpoint
      .tag("users")
      .name("update password")
      .description("Update account password")
      .in("users" / "password")
      .put
      .in(jsonBody[UpdatePasswordRequest])
      .out(jsonBody[User])
      .serverLogic[Task](req =>
        userService
          .updatePassword(req.userName, req.oldPassword, req.newPassword)
          .either
      )

  val delete
      : Full[Unit, Unit, DeleteAccountRequest, Throwable, User, Any, Task] =
    baseEndpoint
      .tag("users")
      .name("delete account")
      .description("Delete your account")
      .in("users")
      .delete
      .in(jsonBody[DeleteAccountRequest])
      .out(jsonBody[User])
      .serverLogic[Task](req =>
        userService.deleteAccount(req.userName, req.password).either
      )

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    register,
    updatePassword,
    delete
  )

}
