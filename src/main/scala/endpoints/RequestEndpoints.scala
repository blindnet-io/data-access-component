package io.blindnet.dataaccess
package endpoints

import objects.*
import services.RequestService

import cats.effect.IO
import io.blindnet.identityclient.auth.*
import io.circe.generic.auto.*
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

class RequestEndpoints(authenticator: AppAuthenticator, service: RequestService) {
  private val base = endpoint.tag("Requests").in("requests")

  val create: ApiEndpoint =
    base.summary("Create a data request")
      .post
      .in(jsonBody[DataRequestPayload])
      .serverLogicSuccess(service.create)

  val list: List[ApiEndpoint] = List(
    create
  )
}
