package endpoints

import domain.api.response.Company
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.zio.jsonBody


trait CompanyEndpoints extends BaseEndpoint {

  // Will probably want to bring in the concept of an admin user...

  val createEndpoint: Endpoint[Unit, Unit, Throwable, Company, Any] =
    baseEndpoint
      .tag("Companies")
      .name("create")
      .description("Create a listing for a Company")
      .post
      .in("companies")
      .out(jsonBody[Company])

  val updateEndpoint: Endpoint[Unit, String, Throwable, Company, Any] =
    baseEndpoint
      .tag("Companies")
      .name("update")
      .description("Update a listing for a Company")
      .put
      .in("companies" / path[String]("id"))
      .out(jsonBody[Company])

  val deleteEndpoint: Endpoint[Unit, String, Throwable, Company, Any] =
    baseEndpoint
      .tag("Companies")
      .name("delete")
      .description("Delete a listing for a Company")
      .delete
      .in("companies" / path[String]("id"))
      .out(jsonBody[Company])

  // Should be paged
  // Probably default to alphabetical, but add by rating.
  // Maybe other sort / search filters
  val getAllEndpoint: Endpoint[Unit, Unit, Throwable, Seq[Company], Any] =
    baseEndpoint
      .tag("Companies")
      .tag("get")
      .description("Get all company listings")
      .get
      .in("companies")
      .out(jsonBody[Seq[Company]])

  val getByIdEndpoint: Endpoint[Unit, String, Throwable, Option[Company], Any] =
    baseEndpoint
      .tag("Companies")
      .name("getById")
      .description("Get a company by it's id")
      .get
      .in("companies" / path[String]("id"))
      .out(jsonBody[Option[Company]])

}
