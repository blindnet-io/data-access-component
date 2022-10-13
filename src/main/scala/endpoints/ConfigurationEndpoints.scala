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

  val list: List[ApiEndpoint] = List(
    getToken,
    resetToken
  )
}
