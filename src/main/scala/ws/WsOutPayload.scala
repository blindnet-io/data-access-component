package io.blindnet.dataaccess
package ws

import io.circe.*
import io.circe.generic.semiauto.*

case class WsOutPayload[T <: WsOutPacket](typ: String, data: T)

object WsOutPayload {
  def apply[T <: WsOutPacket](packet: T): WsOutPayload[T] = new WsOutPayload(packet.typ, packet)
  
  given [T <: WsOutPacket](using Encoder[T]): Encoder[WsOutPayload[T]] = deriveEncoder[WsOutPayload[T]]
}
