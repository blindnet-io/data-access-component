package io.blindnet.dataaccess

import services.*

import cats.effect.*
import cats.implicits.*
import dev.profunktor.redis4cats.*
import dev.profunktor.redis4cats.effect.Log.Stdout.*
import io.blindnet.dataaccess.redis.DataRequestRepository
import org.http4s.HttpApp
import org.http4s.blaze.server.*
import org.http4s.implicits.*
import org.http4s.server.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.syntax.*

class ServerApp {
  def app(router: ServicesRouter): WebSocketBuilder2[IO] => HttpApp[IO] =
    wsb =>
      Router(
        "/v1" -> router.routes(wsb)
      ).orNotFound

  val server: Resource[IO, Server] =
    for {
      redis <- Redis[IO].utf8("redis://127.0.0.1")
      queryRepo = DataRequestRepository(redis)
      connectorService <- Resource.make(ConnectorService(queryRepo))(_ => IO.unit)
      server <- BlazeServerBuilder[IO]
        .bindHttp(Env.get.port, Env.get.host)
        .withHttpWebSocketApp(app(ServicesRouter(redis, queryRepo, connectorService)))
        .resource
    } yield server
}
