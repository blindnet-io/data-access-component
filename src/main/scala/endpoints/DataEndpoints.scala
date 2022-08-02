package io.blindnet.dataaccess
package endpoints

import objects.*
import services.DataService

import cats.effect.IO
import io.circe.generic.auto.*
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

class DataEndpoints(service: DataService) {
  private val base = endpoint.tag("Data").in("data")

  val get: ApiEndpoint =
    base.summary("Get Data")
      .post
      .in("get")
      .in(jsonBody[DataQuery])
      .serverLogicSuccess(service.get)

  val list: List[ApiEndpoint] = List(
    get
  )
}
