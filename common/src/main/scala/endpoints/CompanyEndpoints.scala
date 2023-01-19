package endpoints

import domain.api.request.CreateCompanyRequest
import domain.api.response.Company
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.upickle.jsonBody


trait CompanyEndpoints extends BaseEndpoint {

  val createEndpoint =
    secureBearerEndpoint
      .tag("Companies")
      .name("create")
      .description("Create a listing for a Company")
      .in("companies")
      .post
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])

  val getAllEndpoint: Endpoint[Unit, Unit, Throwable, Seq[Company], Any] =
    baseEndpoint
      .tag("Companies")
      .description("Get all company listings")
      .in("companies")
      .get
      .out(jsonBody[Seq[Company]])

  val getByIdEndpoint: Endpoint[Unit, String, Throwable, Option[Company], Any] =
    baseEndpoint
      .tag("Companies")
      .name("getById")
      .description("Get a company by it's id or slug")
      .in("companies" / path[String]("id"))
      .get
      .out(jsonBody[Option[Company]])

}
