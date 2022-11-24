package io.blindnet.dataaccess
package endpoints.objects

import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class LightConnectorPayload(
  id: UUID,
  name: String,
)

object LightConnectorPayload {
  given Encoder[LightConnectorPayload] = Encoder
    .forProduct2("id", "name")(p => (p.id, p.name))
}
