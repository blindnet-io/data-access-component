package io.blindnet.dataaccess
package endpoints

import objects.*
import services.RequestService

import cats.effect.IO
import io.circe.generic.auto.*
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

class RequestEndpoints(service: RequestService) {
  private val base = endpoint.tag("Requests").in("requests")

  val get: ApiEndpoint =
    base.summary("Create a GET request")
      .post
      .in("get")
      .in(jsonBody[DataQuery])
      .serverLogicSuccess(service.get)

  val list: List[ApiEndpoint] = List(
    get
  )
}
