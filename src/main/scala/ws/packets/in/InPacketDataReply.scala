package io.blindnet.dataaccess
package ws.packets.in

import ws.WsInPacket

import cats.effect.IO
import io.circe.*
import io.circe.generic.semiauto.*

case class InPacketDataReply(request_id: String, typ: DataReplies.DataReply) extends WsInPacket {
  override def handle(): IO[Unit] = IO.println("got reply!")
}

object InPacketDataReply {
  implicit val decoder: Decoder[InPacketDataReply] =
    Decoder.forProduct2("request_id", "type")(InPacketDataReply.apply)
}
