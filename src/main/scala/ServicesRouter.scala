package io.blindnet.dataaccess

import endpoints.*
import services.*

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.tapir.server.http4s.Http4sServerInterpreter

class ServicesRouter {
  private val connectorService = ConnectorService()

  private val connectorEndpoints = ConnectorEndpoints(connectorService)

  val routes: WebSocketBuilder2[IO] => HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toWebSocketRoutes(connectorEndpoints.list)
}
