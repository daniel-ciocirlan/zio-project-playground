package endpoints

import domain.api.request.{DeleteAccountRequest, LoginForm, RegisterAccountRequest, UpdatePasswordRequest}
import domain.api.response.{TokenResponse, User}
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.upickle.jsonBody

trait UserEndpoints extends BaseEndpoint {

  val registerEndpoint: Endpoint[Unit, RegisterAccountRequest, Throwable, User, Any] =
    baseEndpoint
      .tag("Users")
      .name("register")
      .description("Register a user account with username and password")
      .in("users")
      .post
      .in(jsonBody[RegisterAccountRequest])
      .out(jsonBody[User])

  val updatePasswordEndpoint
      : Endpoint[String, UpdatePasswordRequest, Throwable, User, Any] =
    secureBearerEndpoint
      .tag("Users")
      .name("update password")
      .description("Update account password")
      .in("users" / "password")
      .put
      .in(jsonBody[UpdatePasswordRequest])
      .out(jsonBody[User])

  val deleteEndpoint: Endpoint[String, DeleteAccountRequest, Throwable, User, Any] =
    secureBearerEndpoint
      .tag("Users")
      .name("delete account")
      .description("Delete your account")
      .in("users")
      .delete
      .in(jsonBody[DeleteAccountRequest])
      .out(jsonBody[User])

  val generateTokenEndpoint =
    baseEndpoint
      .tag("Users")
      .name("generate token")
      .description("Return a user JWT")
      .post
      .in("users" / "login")
      .in(jsonBody[LoginForm])
      .out(jsonBody[TokenResponse])
}
