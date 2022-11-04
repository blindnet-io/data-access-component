package io.blindnet.dataaccess
package endpoints.objects

import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class ConnectorPayload(
  id: UUID,
  name: String,
  typ: Option[String],
  config: Option[String],
)

object ConnectorPayload {
  given Encoder[ConnectorPayload] = Encoder
    .forProduct4("id", "name", "type", "config")(p => (p.id, p.name, p.typ, p.config))
}
