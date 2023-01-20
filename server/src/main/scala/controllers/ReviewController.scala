package controllers

import domain.api.request.CreateReviewRequest
import domain.api.response.Review
import endpoints.ReviewEndpoints
import services.jwt.{JWTService, ValidatedUserToken}
import services.review.ReviewService
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio._

object ReviewController {
  def makeZIO: ZIO[JWTService with ReviewService, Nothing, ReviewController] =
    for {
      reviewService <- ZIO.service[ReviewService]
      jwtService    <- ZIO.service[JWTService]
    } yield ReviewController(reviewService, jwtService)
}
case class ReviewController(
    reviewService: ReviewService,
    jwtService: JWTService
) extends BaseController
    with ReviewEndpoints {

  val create: Full[
    String,
    ValidatedUserToken,
    CreateReviewRequest,
    Throwable,
    Review,
    Any,
    Task
  ] =
    createEndpoint
      .serverSecurityLogic[ValidatedUserToken, Task](token =>
        jwtService.verifyToken(token).either
      )
      .serverLogic(user => req => reviewService.create(req, user.userId).either)

  val getById: Full[Unit, Unit, Long, Throwable, Option[Review], Any, Task] =
    getByIdEndpoint
      .serverLogic[Task](id => reviewService.getById(id).either)

  val getByCompanyId
      : Full[Unit, Unit, Long, Throwable, Seq[Review], Any, Task] =
    getByCompanyIdEndpoint
      .serverLogic[Task](companyId =>
        reviewService.getByCompanyId(companyId).either
      )

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    create,
    getById,
    getByCompanyId
  )

}
