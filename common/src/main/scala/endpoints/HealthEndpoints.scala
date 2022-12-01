package endpoints

import sttp.tapir._

import java.time.Instant

trait HealthEndpoints extends BaseEndpoint {

  val healthEndpoint: Endpoint[Unit, Unit, Throwable, String, Any] =
    baseEndpoint
      .tag("health")                        // swagger tag
      .name("Health")                       // swagger name
      .description("Health-check endpoint") // swagger description
      .get
      .in("health")
      .out(plainBody[String])

  val timeEndpoint: Endpoint[Unit, Unit, Throwable, Instant, Any] =
    baseEndpoint
      .tag("time")
      .name("currentTime")
      .description("Get the current time")
      .get
      .in("health" / "time")
      .out(plainBody[Instant])

  val secureTimeEndpoint: Endpoint[String, Unit, Throwable, Instant, Any] =
    secureBearerEndpoint
      .tag("time")
      .name("secureTime")
      .description("Get the current time as an authed user")
      .get
      .in("health" / "sec-time")
      .out(plainBody[Instant])
}
