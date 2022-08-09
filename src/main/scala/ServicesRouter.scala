package io.blindnet.dataaccess

import endpoints.*
import redis.*
import services.*

import cats.effect.IO
import dev.profunktor.redis4cats.RedisCommands
import org.http4s.HttpRoutes
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.tapir.server.http4s.Http4sServerInterpreter

class ServicesRouter(redis: RedisCommands[IO, String, String], queryRepository: QueryRepository, connectorService: ConnectorService) {
  private val dataService = DataService(queryRepository)
  private val requestService = RequestService(connectorService, queryRepository)

  private val connectorEndpoints = ConnectorEndpoints(connectorService)
  private val dataEndpoints = DataEndpoints(dataService)
  private val requestEndpoints = RequestEndpoints(requestService)

  private val allEndpoints = List(
    connectorEndpoints.list,
    dataEndpoints.list,
    requestEndpoints.list,
  ).flatten

  val routes: WebSocketBuilder2[IO] => HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toWebSocketRoutes(allEndpoints)
}
