package io.blindnet.dataaccess

import cats.effect.{IO, Resource}
import org.http4s.blaze.server.*
import org.http4s.HttpApp
import org.http4s.implicits.*
import org.http4s.server.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.syntax.*

class ServerApp {
  def app(): IO[WebSocketBuilder2[IO] => HttpApp[IO]] =
    ServicesRouter().map(router => {
      wsb =>
        Router(
          "/v1" -> router.routes(wsb)
        ).orNotFound
    })

  val server: Resource[IO, Server] =
    for {
      app <- Resource.make(app())(_ => IO.unit)
      server <- BlazeServerBuilder[IO]
        .bindHttp(8028, "127.0.0.1")
        .withHttpWebSocketApp(app)
        .resource
    } yield server
}
