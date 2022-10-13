package io.blindnet.dataaccess
package endpoints

import services.DataService

import cats.effect.IO
import io.circe.generic.auto.*
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.HeaderNames
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

import java.util.UUID

class DataEndpoints(service: DataService) {
  private val base = endpoint.tag("Data").in("data")

  val get: ApiEndpoint =
    base.summary("Get data")
      .get
      .in(path[UUID]("app_id") / path[String]("request_id") / path[String]("data_id"))
      .out(streamBinaryBody(Fs2Streams[IO])(CodecFormat.OctetStream()))
      .out(header(HeaderNames.ContentDisposition, "attachment"))
      .serverLogicSuccess(service.get)

  val list: List[ApiEndpoint] = List(
    get
  )
}
