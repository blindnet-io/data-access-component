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

case class WsConnection(queue: Queue[IO, String]) {
  def receive(raw: String): IO[Unit] =
    def parsePacket[T <: WsInPacket](): Option[T] = for {
      payload <- parse(raw).toOption.flatMap(_.asObject)
      typ <- payload("typ").flatMap(_.asString)
      decoder <- WsInPacket.decoders.get(typ)
      packet <- payload("data").flatMap(_.as[T](decoder.asInstanceOf[Decoder[T]]).toOption)
    } yield packet

    for {
      _ <- IO.println("raw: " + raw)
      _ <- parsePacket[WsInPacket]() match
        case Some(packet) => packet.handle()
        case None => IO.println("ignoring invalid WS packet")
    } yield ()

  def send[T <: WsOutPacket](payload: T)(implicit enc: Encoder[T]): IO[Unit] =
    queue.offer(WsOutPayload(payload).asJson.noSpaces)
}
