package io.blindnet.dataaccess
package ws.packets.in

import ws.WsInPacket

import cats.effect.IO
import io.circe.*
import io.circe.generic.semiauto.*

import java.nio.ByteBuffer
import java.util.Base64

case class InPacketData(request_id: String) extends WsInPacket {
  override def handle(remaining: ByteBuffer): IO[Unit] = {
    val data = new Array[Byte](remaining.remaining())
    remaining.get(data)

    IO.println("got data! " + String(data))
  }
}

object InPacketData {
  implicit val byteArrayDecoder: Decoder[Array[Byte]] =
    Decoder.decodeString.map(Base64.getDecoder.decode)
  implicit val decoder: Decoder[InPacketData] = deriveDecoder[InPacketData]
}
