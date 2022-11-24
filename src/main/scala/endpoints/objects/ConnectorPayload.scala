package io.blindnet.dataaccess
package endpoints.objects

import models.{Connector, CustomConnector, GlobalConnector}

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
  def apply(co: Connector): ConnectorPayload = co match
    case GlobalConnector(id, _, name, typ, config) => new ConnectorPayload(id, name, Some(typ), config)
    case CustomConnector(id, _, name, _) => new ConnectorPayload(id, name, None, None)

  given Encoder[ConnectorPayload] = Encoder
    .forProduct4("id", "name", "type", "config")(p => (p.id, p.name, p.typ, p.config))
}
