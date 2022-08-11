package io.blindnet.dataaccess
package ws.packets.out

import ws.WsOutPacket

import io.blindnet.dataaccess.endpoints.objects.DataRequestPayload
import io.blindnet.dataaccess.models.DataRequestActions
import io.circe.*
import io.circe.generic.semiauto.*

case class OutPacketDataRequest(query: DataRequestPayload, action: DataRequestActions.DataRequestAction) extends WsOutPacket {
  override def typ: String = "data_query"
}

implicit val eOutPacketDataQuery: Encoder[OutPacketDataRequest] = deriveEncoder[OutPacketDataRequest]
