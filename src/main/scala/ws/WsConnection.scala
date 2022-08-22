package io.blindnet.dataaccess
package ws

import redis.DataRequestRepository
import ws.*

import cats.data.*
import cats.effect.*
import cats.effect.std.Queue
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*

import java.nio.ByteBuffer

case class WsConnection(repos: Repositories, queue: Queue[IO, String]) {
  def receive(raw: String): IO[Unit] =
    def parsePacket[T <: WsInPacket](): Option[T] =
      for {
        payload <- parse(raw).toOption.flatMap(_.asObject)
        typ <- payload("typ").flatMap(_.asString)
        decoder <- WsInPacket.decoders.get(typ)
        packet <- payload("data").flatMap(_.as[T](decoder.asInstanceOf[Decoder[T]]).toOption)
      } yield packet

    for {
      _ <- parsePacket[WsInPacket]() match
        case Some(packet) => packet.handle(this)
        case None => IO.println("ignoring invalid WS packet")
    } yield ()

  def send[T <: WsOutPacket](packet: T)(implicit enc: Encoder[T]): IO[Unit] =
    queue.offer(WsOutPayload(packet).asJson.noSpaces)
}
