package io.blindnet.dataaccess

import endpoints.*
import services.*

import cats.effect.IO
import dev.profunktor.redis4cats.RedisCommands
import io.blindnet.identityclient.auth.*
import org.http4s.HttpRoutes
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

class Services(repos: Repositories, connectorService: ConnectorService) {
  private val appAuthenticator = StAuthenticator(repos.apiTokens)
  
  private val dataService = DataService(repos)
  private val requestService = RequestService(connectorService, repos)

  private val connectorEndpoints = ConnectorEndpoints(connectorService)
  private val dataEndpoints = DataEndpoints(dataService)
  private val requestEndpoints = RequestEndpoints(appAuthenticator, requestService)

  private val apiEndpoints = List(
    connectorEndpoints.list,
    dataEndpoints.list,
    requestEndpoints.list,
  ).flatten

  private val swaggerEndpoints = SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.pathPrefix(List("swagger")))
    .fromServerEndpoints[IO](apiEndpoints, "Data Access API", Env.get.name)

  val routes: WebSocketBuilder2[IO] => HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toWebSocketRoutes(apiEndpoints ++ swaggerEndpoints)
}

object Services {
  def apply(repos: Repositories): IO[Services] = ConnectorService(repos).map(new Services(repos, _))
}
