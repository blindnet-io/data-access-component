package io.blindnet.dataaccess
package endpoints

import services.ConnectorService

import cats.effect.IO
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.json.circe.*

class ConnectorEndpoints(service: ConnectorService) {
  private val base = endpoint.tag("Connectors").in("connectors")

  val ws: ApiEndpoint =
    base.summary("Establish WebSocket connection")
      .get
      .in("ws" / path[String]("app_id"))
      .out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain](Fs2Streams[IO]))
      .serverLogicSuccess(service.ws)

  val sendMainData: ApiEndpoint =
    base.summary("Send main data")
      .post
      .in("data" / path[String]("app_id") / path[String]("request_id") / "main")
      .in(query[Boolean]("last"))
      .in(streamBinaryBody(Fs2Streams[IO])(CodecFormat.OctetStream()))
      .serverLogicSuccess(service.sendMainData)

  val sendAdditionalData: ApiEndpoint =
    base.summary("Send additional data")
      .post
      .in("data" / path[String]("app_id") / path[String]("request_id") / "additional")
      .in(streamBinaryBody(Fs2Streams[IO])(CodecFormat.OctetStream()))
      .out(jsonBody[String])
      .serverLogicSuccess(service.sendAdditionalData)

  val list: List[ApiEndpoint] = List(
    ws,
    sendMainData,
    sendAdditionalData
  )
}
