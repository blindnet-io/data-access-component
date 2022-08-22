package io.blindnet.dataaccess
package ws

import ws.packets.in.*

import cats.effect.IO
import io.circe.Decoder

import java.nio.ByteBuffer

trait WsInPacket {
  def handle(conn: WsConnection): IO[Unit]
}

object WsInPacket {
  val decoders: Map[String, Decoder[_ <: WsInPacket]] = Map(
    "data_request_reply" -> InPacketDataRequestReply.decoder,
  )
}
