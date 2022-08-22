package io.blindnet.dataaccess
package ws.packets.out

import endpoints.objects.DataRequestPayload
import models.DataRequestAction
import ws.WsOutPacket

import io.circe.*
import io.circe.generic.semiauto.*

case class OutPacketDataRequest(request: DataRequestPayload) extends WsOutPacket {
  override def typ: String = "data_request"
}

implicit val eOutPacketDataQuery: Encoder[OutPacketDataRequest] = deriveEncoder[OutPacketDataRequest]
