package io.blindnet.dataaccess
package ws.packets.out

import objects.DataQuery
import ws.WsOutPacket

import io.circe.*
import io.circe.generic.semiauto.*

case class OutPacketDataQuery(query: DataQuery, action: DataActions.DataAction) extends WsOutPacket {
  override def typ: String = "data_query"
}

implicit val eOutPacketDataQuery: Encoder[OutPacketDataQuery] = deriveEncoder[OutPacketDataQuery]
