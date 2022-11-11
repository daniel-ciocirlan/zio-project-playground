package endpoints

import sttp.tapir.{Endpoint, plainBody}

trait HealthEndpoints extends BaseEndpoint {

  val healthEndpoint: Endpoint[Unit, Unit, Throwable, String, Any] =
    baseEndpoint
      .tag("health")                        // swagger tag
      .name("Health")                       // swagger name
      .description("Health-check endpoint") // swagger description
      .get
      .in("health")
      .out(plainBody[String])

}
