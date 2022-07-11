package io.blindnet.dataaccess
package endpoints

import services.ConnectorService

import cats.effect.IO
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*

class ConnectorEndpoints(service: ConnectorService) {
  private val base = endpoint.tag("Connectors").in("connectors")

  val ws: ApiEndpoint =
    base.summary("Establish WebSocket connection")
      .get
      .in("ws")
      .out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain](Fs2Streams[IO]))
      .serverLogicSuccess(service.ws)

  val list: List[ApiEndpoint] = List(
    ws
  )
}
