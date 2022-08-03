package io.blindnet.dataaccess
package ws.packets

import objects.DataQuery

import io.blindnet.dataaccess.ws.WsPacket
import io.circe.*
import io.circe.generic.semiauto.*

case class DataQueryPacket(query: DataQuery, action: DataActions.DataAction) extends WsPacket {
  override def typ: String = "data_query"
}
implicit val eDataQueryPacket: Encoder[DataQueryPacket] = deriveEncoder[DataQueryPacket]
