package io.blindnet.dataaccess
package ws.packets.out

import endpoints.objects.DataRequestPayload
import models.DataRequestAction
import ws.WsOutPacket

import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class OutPacketWelcome(app_id: Option[UUID], connector_id: Option[UUID], connector_name: Option[String]) extends WsOutPacket {
  override def typ: String = "welcome"
}

object OutPacketWelcome {
  given Encoder[OutPacketWelcome] = deriveEncoder[OutPacketWelcome]

  def apply(app_id: UUID, connector_id: UUID, connector_name: String): OutPacketWelcome =
    new OutPacketWelcome(Some(app_id), Some(connector_id), Some(connector_name))

  def apply(): OutPacketWelcome =
    new OutPacketWelcome(None, None, None)
}
