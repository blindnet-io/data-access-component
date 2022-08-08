package io.blindnet.dataaccess
package ws

import ws.*
import ws.packets.in.InPacketDataReply

import cats.data.*
import cats.effect.*
import cats.effect.std.Queue
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*

import java.nio.ByteBuffer

case class WsConnection(queue: Queue[IO, Array[Byte]]) {
  def receive(raw: ByteBuffer): IO[Unit] =
    def parsePacket[T <: WsInPacket](): Option[T] =
      val jsonLen = raw.getInt
      val jsonBytes = new Array[Byte](jsonLen)
      raw.get(jsonBytes)
      val json = String(jsonBytes)

      for {
        payload <- parse(json).toOption.flatMap(_.asObject)
        typ <- payload("typ").flatMap(_.asString)
        decoder <- WsInPacket.decoders.get(typ)
        packet <- payload("data").flatMap(_.as[T](decoder.asInstanceOf[Decoder[T]]).toOption)
      } yield packet

    for {
      _ <- parsePacket[WsInPacket]() match
        case Some(packet) => packet.handle(raw)
        case None => IO.println("ignoring invalid WS packet")
    } yield ()

  def send[T <: WsOutPacket](payload: T)(implicit enc: Encoder[T]): IO[Unit] =
    queue.offer(WsOutPayload(payload).asJson.noSpaces.getBytes)
}
