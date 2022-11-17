package controllers

import domain.api.request.{
  DeleteAccountRequest,
  LoginForm,
  RegisterAccountRequest,
  UpdatePasswordRequest
}
import domain.api.response.{TokenResponse, User}
import domain.errors.UnauthorizedException
import endpoints.UserEndpoints
import services.jwt.{JWTService, ValidatedUserToken}
import services.user.UserService
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio._

object UserController {
  def makeZIO: ZIO[UserService with JWTService, Nothing, UserController] = for {
    userService <- ZIO.service[UserService]
    jwtService  <- ZIO.service[JWTService]
  } yield UserController(userService, jwtService)
}

case class UserController(userService: UserService, jwtService: JWTService)
    extends BaseController
    with UserEndpoints {

  val register
      : Full[Unit, Unit, RegisterAccountRequest, Throwable, User, Any, Task] =
    registerEndpoint
      .serverLogic[Task](req =>
        userService.registerUser(req.userName, req.password).either
      )

  val updatePassword: Full[
    String,
    ValidatedUserToken,
    UpdatePasswordRequest,
    Throwable,
    User,
    Any,
    Task
  ] =
    updatePasswordEndpoint
      .serverSecurityLogic[ValidatedUserToken, Task](token =>
        jwtService.verifyToken(token).either
      )
      .serverLogic(vToken =>
        req =>
          userService
            .updatePassword(vToken.userName, req.oldPassword, req.newPassword)
            .either
      )

  val delete: Full[
    String,
    ValidatedUserToken,
    DeleteAccountRequest,
    Throwable,
    User,
    Any,
    Task
  ] =
    deleteEndpoint
      .serverSecurityLogic[ValidatedUserToken, Task](token =>
        jwtService.verifyToken(token).either
      )
      .serverLogic(token =>
        req => userService.deleteAccount(token.userName, req.password).either
      )

  val generateToken
      : Full[Unit, Unit, LoginForm, Throwable, TokenResponse, Any, Task] =
    generateTokenEndpoint
      .serverLogic[Task](loginForm =>
        userService
          .generateToken(loginForm.username, loginForm.password)
          .someOrFail(UnauthorizedException)
          .either
      )

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    register,
    updatePassword,
    delete,
    generateToken
  )

}
