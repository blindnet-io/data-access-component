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

  val getNamespaces: ApiEndpoint =
    base.summary("Get the namespaces defined for this app")
      .get
      .in("namespaces")
      .out(jsonBody[List[NamespacePayload]])
      .serverLogicSuccess(service.getNamespaces)

  val getNamespace: ApiEndpoint =
    base.summary("Get information about a namespace")
      .get
      .in("namespaces" / path[UUID]("namespace"))
      .out(jsonBody[NamespacePayload])
      .serverLogicSuccess(service.getNamespace)

  val getNamespaceToken: ApiEndpoint =
    base.summary("Get the current API token for a namespace")
      .get
      .in("namespaces" / path[UUID]("namespace") / "token")
      .out(jsonBody[String])
      .serverLogicSuccess(service.getNamespaceToken)

  val resetNamespaceToken: ApiEndpoint =
    base.summary("Invalidate the current API token of a namespace and create a new one")
      .post
      .in("namespaces" / path[UUID]("namespace") / "token" / "reset")
      .out(jsonBody[String])
      .serverLogicSuccess(service.resetNamespaceToken)

  val list: List[ApiEndpoint] = List(
    getToken,
    resetToken,
    getNamespaces,
    getNamespace,
    getNamespaceToken,
    resetNamespaceToken
  )
}
