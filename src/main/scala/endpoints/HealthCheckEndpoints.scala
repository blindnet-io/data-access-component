package io.blindnet.dataaccess
package endpoints

import cats.effect.*
import sttp.tapir.*
import sttp.tapir.server.http4s.*

class HealthCheckEndpoints() {
  val base = endpoint.tag("Health")

  val health: ApiEndpoint =
    base
      .description("Is the app running?")
      .get
      .in("health")
      .serverLogicSuccess(_ => IO.unit)

  val list: List[ApiEndpoint] = List(health)
}
