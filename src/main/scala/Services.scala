package io.blindnet.dataaccess

import endpoints.*
import endpoints.auth.*
import errors.ErrorHandler
import models.App
import services.*

import cats.effect.IO
import dev.profunktor.redis4cats.RedisCommands
import io.blindnet.identityclient.IdentityClient
import io.blindnet.identityclient.auth.*
import org.http4s.HttpRoutes
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

class Services(repos: Repositories, env: Env, connectorService: ConnectorService, identityClient: IdentityClient) {
  private val configurationService = ConfigurationService(repos)
  private val dataService = DataService(repos)
  private val requestService = RequestService(connectorService, repos)

  private val appAuthenticator = StAuthenticator(repos.apps)
  private val connectorAuthenticator = StAuthenticator(repos.connectors)
  private val jwtAuthenticator = JwtAuthenticator(identityClient)
  private val jwtAppAuthenticator = JwtAppAuthenticator(repos, jwtAuthenticator)
  private val jwtIdentityAuthenticator = JwtIdentityAuthenticator(repos, JwtLocalAuthenticator(env.identityKey))

  private val healthEndpoints = HealthCheckEndpoints()
  private val configurationEndpoints = ConfigurationEndpoints(jwtAppAuthenticator, jwtIdentityAuthenticator, configurationService)
  private val connectorEndpoints = ConnectorEndpoints(connectorAuthenticator, connectorService)
  private val dataEndpoints = DataEndpoints(dataService)
  private val requestEndpoints = RequestEndpoints(appAuthenticator, requestService)

  private val apiEndpoints = List(
    healthEndpoints.list,
    configurationEndpoints.list,
    connectorEndpoints.list,
    dataEndpoints.list,
    requestEndpoints.list,
  ).flatten

  private val swaggerEndpoints = SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.pathPrefix(List("swagger")))
    .fromServerEndpoints[IO](apiEndpoints, "Data Access API", Env.get.name)

  private val http4sOptions = Http4sServerOptions
    .customiseInterceptors[IO]
    .serverLog(None)
    .exceptionHandler(None)
    .options

  val routes: WebSocketBuilder2[IO] => HttpRoutes[IO] = wsb =>
    CORS.policy.withAllowOriginAll(
      ErrorHandler(env)(
        Http4sServerInterpreter[IO](http4sOptions).toWebSocketRoutes(
          apiEndpoints ++ swaggerEndpoints)(wsb)))
}

object Services {
  def apply(repos: Repositories, env: Env, identityClient: IdentityClient): IO[Services] = ConnectorService(repos).map(new Services(repos, env, _, identityClient))
}
