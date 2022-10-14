package io.blindnet.dataaccess

import endpoints.*
import models.App
import services.*

import cats.effect.IO
import dev.profunktor.redis4cats.RedisCommands
import io.blindnet.identityclient.IdentityClient
import io.blindnet.identityclient.auth.*
import org.http4s.HttpRoutes
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

class Services(repos: Repositories, connectorService: ConnectorService, identityClient: IdentityClient) {
  private val appAuthenticator = StAuthenticator(repos.apps)
  private val jwtAuthenticator = JwtAuthenticator(identityClient)
  private val jwtAppAuthenticator = jwtAuthenticator.requireAppJwt.mapJwtF(jwt =>
    repos.apps.findById(jwt.appId).flatMap(_ match
      case Some(app) => IO.pure(app)
      case None => for {
        token <- configurationService.generateStaticToken()
        app = App(jwt.appId, token)
        _ <- repos.apps.insert(app)
      } yield app))
  private val namespaceAuthenticator = StAuthenticator(repos.namespaces)

  private val configurationService = ConfigurationService(repos)
  private val dataService = DataService(repos)
  private val requestService = RequestService(connectorService, repos)

  private val configurationEndpoints = ConfigurationEndpoints(jwtAppAuthenticator, configurationService)
  private val connectorEndpoints = ConnectorEndpoints(namespaceAuthenticator, connectorService)
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
  def apply(repos: Repositories, identityClient: IdentityClient): IO[Services] = ConnectorService(repos).map(new Services(repos, _, identityClient))
}
