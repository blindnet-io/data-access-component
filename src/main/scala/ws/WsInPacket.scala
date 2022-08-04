package io.blindnet.dataaccess
package ws

import ws.packets.in.*

import cats.effect.IO
import io.circe.Decoder

trait WsInPacket {
  def handle(): IO[Unit]
}

object WsInPacket {
  val decoders: Map[String, Decoder[_ <: WsInPacket]] = Map(
    "data_reply" -> InPacketDataReply.decoder
  )
}
