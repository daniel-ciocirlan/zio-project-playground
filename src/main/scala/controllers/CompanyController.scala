package controllers

import sttp.tapir._
import zio.ZIO

object CompanyController {
  def makeZIO: ZIO[Any, Nothing, CompanyController] =
    ZIO.succeed(CompanyController())
}

case class CompanyController() extends BaseController {

  // Will probably want to bring in the concept of an admin user...

  val create =
    baseEndpoint
      .tag("Companies")
      .name("create")
      .description("Create a listing for a Company")
      .in("companies")
      .post

  val update =
    baseEndpoint
      .tag("Companies")
      .name("update")
      .description("Update a listing for a Company")
      .in("companies" / path[String]("id"))
      .put

  val delete =
    baseEndpoint
      .tag("Companies")
      .name("delete")
      .description("Delete a listing for a Company")
      .in("companies" / path[String]("id"))
      .delete

  // Should be paged
  // Probably default to alphabetical, but add by rating.
  // Maybe other sort / search filters
  val get =
    baseEndpoint
      .tag("Companies")
      .tag("get")
      .description("Get all company listings")
      .in("companies")
      .get

  val getById =
    baseEndpoint
      .tag("Companies")
      .name("getById")
      .description("Get a company by it's id")
      .in("companies" / path[String]("id"))
      .get


}
