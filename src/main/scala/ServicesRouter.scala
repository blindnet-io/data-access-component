package io.blindnet.dataaccess

import endpoints.*
import services.*

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.tapir.server.http4s.Http4sServerInterpreter

class ServicesRouter(connectorService: ConnectorService) {
  private val dataService = DataService(connectorService)

  private val connectorEndpoints = ConnectorEndpoints(connectorService)
  private val dataEndpoints = DataEndpoints(dataService)

  private val allEndpoints = List(
    connectorEndpoints.list,
    dataEndpoints.list,
  ).flatten

  val routes: WebSocketBuilder2[IO] => HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toWebSocketRoutes(allEndpoints)
}

object ServicesRouter {
  def apply(): IO[ServicesRouter] =
    ConnectorService().map(cs => new ServicesRouter(cs))
}
