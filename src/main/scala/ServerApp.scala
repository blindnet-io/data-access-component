package io.blindnet.dataaccess

import services.*

import cats.effect.*
import cats.implicits.*
import dev.profunktor.redis4cats.*
import dev.profunktor.redis4cats.effect.Log.Stdout.*
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
      redis <- Redis[IO].utf8("redis://localhost")
      connectorService <- Resource.make(ConnectorService())(_ => IO.unit)
      server <- BlazeServerBuilder[IO]
        .bindHttp(8028, "127.0.0.1")
        .withHttpWebSocketApp(app(ServicesRouter(redis, connectorService)))
        .resource
    } yield server
}
