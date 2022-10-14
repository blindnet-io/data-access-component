package io.blindnet.dataaccess
package endpoints

import endpoints.auth.NamespaceAuthenticator
import services.ConnectorService

import cats.effect.IO
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.json.circe.*

import java.util.UUID

class ConnectorEndpoints(authenticator: NamespaceAuthenticator, service: ConnectorService) {
  private val base = authenticator.withBaseEndpoint(endpoint.tag("Connectors").in("connectors")).secureEndpoint

  val ws: ApiEndpoint =
    base.summary("Establish WebSocket connection")
      .get
      .in("ws")
      .out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain](Fs2Streams[IO]))
      .serverLogicSuccess(service.ws)

  val sendMainData: ApiEndpoint =
    base.summary("Send main data")
      .post
      .in("data" / path[String]("request_id") / "main")
      .in(query[Boolean]("last"))
      .in(streamBinaryBody(Fs2Streams[IO])(CodecFormat.OctetStream()))
      .serverLogicSuccess(service.sendMainData)

  val sendAdditionalData: ApiEndpoint =
    base.summary("Send additional data")
      .post
      .in("data" / path[String]("request_id") / "additional")
      .in(streamBinaryBody(Fs2Streams[IO])(CodecFormat.OctetStream()))
      .out(jsonBody[String])
      .serverLogicSuccess(service.sendAdditionalData)

  val list: List[ApiEndpoint] = List(
    ws,
    sendMainData,
    sendAdditionalData
  )
}
