package endpoints

import domain.api.request.{
  DeleteAccountRequest,
  RegisterRequest,
  UpdatePasswordRequest
}
import domain.api.response.User
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.zio.jsonBody

trait UserEndpoints extends BaseEndpoint {

  val registerEndpoint: Endpoint[Unit, RegisterRequest, Throwable, User, Any] =
    baseEndpoint
      .tag("users")
      .name("register")
      .description("Register a user account with username and password")
      .in("users")
      .post
      .in(jsonBody[RegisterRequest])
      .out(jsonBody[User])

  val updatePasswordEndpoint
      : Endpoint[Unit, UpdatePasswordRequest, Throwable, User, Any] =
    baseEndpoint
      .tag("users")
      .name("update password")
      .description("Update account password")
      .in("users" / "password")
      .put
      .in(jsonBody[UpdatePasswordRequest])
      .out(jsonBody[User])

  val deleteEndpoint: Endpoint[Unit, DeleteAccountRequest, Throwable, User, Any] =
    baseEndpoint
      .tag("users")
      .name("delete account")
      .description("Delete your account")
      .in("users")
      .delete
      .in(jsonBody[DeleteAccountRequest])
      .out(jsonBody[User])

}
