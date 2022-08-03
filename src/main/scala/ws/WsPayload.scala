package io.blindnet.dataaccess
package ws

import io.circe.*
import io.circe.generic.semiauto.*

case class WsPayload[T <: WsPacket](typ: String, packet: T)

object WsPayload {
  def apply[T <: WsPacket](packet: T): WsPayload[T] = new WsPayload(packet.typ, packet)
}

implicit def eWsPayload[T <: WsPacket](implicit encoder: Encoder[T]): Encoder[WsPayload[T]] = deriveEncoder[WsPayload[T]]
