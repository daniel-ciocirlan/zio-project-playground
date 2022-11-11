package controllers

import endpoints.CompanyEndpoints
import zio.ZIO

object CompanyController {
  def makeZIO: ZIO[Any, Nothing, CompanyController] =
    ZIO.succeed(CompanyController())
}

case class CompanyController() extends BaseController with CompanyEndpoints {

  // Will probably want to bring in the concept of an admin user...

  val create =
    createEndpoint
      .serverLogic(???)

  val update =
    updateEndpoint
      .serverLogic(???)

  val delete =
    deleteEndpoint
      .serverLogic(???)

  // Should be paged
  // Probably default to alphabetical, but add by rating.
  // Maybe other sort / search filters
  val get =
    getAllEndpoint
      .serverLogic(???)

  val getById =
    getByIdEndpoint
      .serverLogic(???)

}
