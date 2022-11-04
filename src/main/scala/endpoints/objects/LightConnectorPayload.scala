package io.blindnet.dataaccess
package endpoints.objects

import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class LightConnectorPayload(
  id: UUID,
  name: String,
  typ: Option[String],
)

object LightConnectorPayload {
  given Encoder[LightConnectorPayload] = Encoder
    .forProduct3("id", "name", "type")(p => (p.id, p.name, p.typ))
}
