package io.blindnet.dataaccess
package ws

import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class WsOutGlobalPayload[T <: WsOutPacket](
  app_id: UUID,
  connector_id: UUID,
  connector_type: String,
  connector_config: Option[String],
  typ: String,
  data: T
)

object WsOutGlobalPayload {
  def apply[T <: WsOutPacket](app_id: UUID, connector_id: UUID, connector_type: String, connector_config: Option[String], packet: T): WsOutGlobalPayload[T] =
    new WsOutGlobalPayload(app_id, connector_id, connector_type, connector_config, packet.typ, packet)
}

implicit def eWsOutGlobalPayload[T <: WsOutPacket](implicit encoder: Encoder[T]): Encoder[WsOutGlobalPayload[T]] =
  deriveEncoder[WsOutGlobalPayload[T]]
