package io.blindnet.dataaccess
package endpoints

import endpoints.auth.ConnectorAuthenticator
import services.ConnectorService

import cats.effect.IO
import io.blindnet.identityclient.auth.ConstAuthenticator
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.*

import java.util.UUID

class ConnectorEndpoints(authenticator: ConnectorAuthenticator, service: ConnectorService) {
  private val publicBase = endpoint.tag("Connectors").in("connectors")
  private val customBase = authenticator.withBaseEndpoint(publicBase).secureEndpoint
  private val globalBase = ConstAuthenticator(Env.get.globalConnectorToken, IO.pure(())).withBaseEndpoint(publicBase).secureEndpoint
  private val dualBase = publicBase
    .securityIn(header[Option[String]]("Authorization"))
    .securityIn(header[Option[String]]("X-Application-ID"))
    .securityIn(header[Option[String]]("X-Connector-ID"))
    .errorOut(statusCode)
    .errorOut(jsonBody[String])
    .serverSecurityLogic(service.dualAuth(authenticator).tupled(_).map(_.left.map((StatusCode.Unauthorized, _))))

  val wsCustom: ApiEndpoint =
    customBase.summary("Establish WebSocket connection (custom connector)")
      .get
      .in("ws" / "custom")
      .out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain](Fs2Streams[IO]))
      .serverLogicSuccess(service.wsCustom)

  val wsGlobal: ApiEndpoint =
    globalBase.summary("Establish WebSocket connection (global connector)")
      .get
      .in("ws" / "global")
      .out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain](Fs2Streams[IO]))
      .serverLogicSuccess(service.wsGlobal)

  val sendMainData: ApiEndpoint =
    dualBase.summary("Send main data")
      .post
      .in("data" / path[String]("request_id") / "main")
      .in(query[Boolean]("last"))
      .in(streamBinaryBody(Fs2Streams[IO])(CodecFormat.OctetStream()))
      .serverLogicSuccess(service.sendMainData)

  val sendAdditionalData: ApiEndpoint =
    dualBase.summary("Send additional data")
      .post
      .in("data" / path[String]("request_id") / "additional")
      .in(streamBinaryBody(Fs2Streams[IO])(CodecFormat.OctetStream()))
      .out(jsonBody[String])
      .serverLogicSuccess(service.sendAdditionalData)

  val list: List[ApiEndpoint] = List(
    wsCustom,
    wsGlobal,
    sendMainData,
    sendAdditionalData
  )
}
