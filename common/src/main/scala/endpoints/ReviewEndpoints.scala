package endpoints

import domain.api.request.CreateReviewRequest
import domain.api.response.Review
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir._

trait ReviewEndpoints extends BaseEndpoint {

  val createEndpoint: Endpoint[String, CreateReviewRequest, Throwable, Review, Any] =
    secureBearerEndpoint
      .tag("Reviews")
      .name("create")
      .description("Add a review to a Company")
      .in("reviews")
      .post
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getByIdEndpoint: Endpoint[Unit, Long, Throwable, Option[Review], Any] =
    baseEndpoint
      .tag("Reviews")
      .name("getById")
      .description("Get a review by its id")
      .in("reviews" / path[Long]("id"))
      .get
      .out(jsonBody[Option[Review]])

  val getByCompanyIdEndpoint: Endpoint[Unit, Long, Throwable, Seq[Review], Any] =
    baseEndpoint
      .tag("Reviews")
      .name("getByCompanyId")
      .description("Get reviews by company id")
      .in("reviews" / "company" / path[Long]("id"))
      .get
      .out(jsonBody[Seq[Review]])

}
