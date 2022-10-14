package io.blindnet.dataaccess

import services.*

import cats.effect.*
import cats.implicits.*
import dev.profunktor.redis4cats.*
import dev.profunktor.redis4cats.effect.Log.Stdout.*
import io.blindnet.identityclient.IdentityClientBuilder
import org.http4s.HttpApp
import org.http4s.blaze.server.*
import org.http4s.implicits.*
import org.http4s.server.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.syntax.*

class ServerApp {
  def app(services: Services): WebSocketBuilder2[IO] => HttpApp[IO] =
    wsb =>
      Router(
        "/v1" -> services.routes(wsb)
      ).orNotFound

  val server: Resource[IO, Server] =
    for {
      repos <- Repositories()
      identityClient <- IdentityClientBuilder().withBaseUri(Env.get.identityUrl).resource
      services <- Resource.eval(Services(repos, identityClient))
      server <- BlazeServerBuilder[IO]
        .bindHttp(Env.get.port, Env.get.host)
        .withHttpWebSocketApp(app(services))
        .resource
    } yield server
}
