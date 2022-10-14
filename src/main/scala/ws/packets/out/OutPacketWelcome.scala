package io.blindnet.dataaccess
package ws.packets.out

import endpoints.objects.DataRequestPayload
import models.DataRequestAction
import ws.WsOutPacket

import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class OutPacketWelcome(app_id: UUID, namespace_id: UUID, namespace_name: String) extends WsOutPacket {
  override def typ: String = "welcome"
}

object OutPacketWelcome {
  given Encoder[OutPacketWelcome] = deriveEncoder[OutPacketWelcome]
}
