package io.blindnet.dataaccess
package ws

import ws.packets.in.*

import cats.effect.IO
import io.circe.Decoder

import java.nio.ByteBuffer

trait WsInPacket {
  def handle(remaining: ByteBuffer): IO[Unit]
}

object WsInPacket {
  val decoders: Map[String, Decoder[_ <: WsInPacket]] = Map(
    "data" -> InPacketData.decoder,
    "data_reply" -> InPacketDataReply.decoder,
  )
}
