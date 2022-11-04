package io.blindnet.dataaccess
package endpoints

import endpoints.auth.JwtAppAuthenticator
import endpoints.objects.*
import services.ConfigurationService

import cats.effect.IO
import io.blindnet.identityclient.auth.*
import io.circe.generic.auto.*
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

import java.util.UUID

class ConfigurationEndpoints(authenticator: JwtAppAuthenticator, service: ConfigurationService) {
  private val base = authenticator.withBaseEndpoint(endpoint.tag("Configuration").in("configuration")).secureEndpoint

  val getToken: ApiEndpoint =
    base.summary("Get the current API token for this app")
      .get
      .in("token")
      .out(jsonBody[String])
      .serverLogicSuccess(service.getToken)

  val resetToken: ApiEndpoint =
    base.summary("Invalidate the current API token of this app and create a new one")
      .post
      .in("token" / "reset")
      .out(jsonBody[String])
      .serverLogicSuccess(service.resetToken)

  val getConnectors: ApiEndpoint =
    base.summary("Get the connectors defined for this app")
      .get
      .in("connectors")
      .out(jsonBody[List[ConnectorPayload]])
      .serverLogicSuccess(service.getConnectors)

  val getConnector: ApiEndpoint =
    base.summary("Get information about a connector")
      .get
      .in("connectors" / path[UUID]("connector"))
      .out(jsonBody[ConnectorPayload])
      .serverLogicSuccess(service.getConnector)

  val getConnectorToken: ApiEndpoint =
    base.summary("Get the current API token for a connector")
      .get
      .in("connectors" / path[UUID]("connector") / "token")
      .out(jsonBody[String])
      .serverLogicSuccess(service.getConnectorToken)

  val resetConnectorToken: ApiEndpoint =
    base.summary("Invalidate the current API token of a connector and create a new one")
      .post
      .in("connectors" / path[UUID]("connector") / "token" / "reset")
      .out(jsonBody[String])
      .serverLogicSuccess(service.resetConnectorToken)

  val list: List[ApiEndpoint] = List(
    getToken,
    resetToken,
    getConnectors,
    getConnector,
    getConnectorToken,
    resetConnectorToken
  )
}
