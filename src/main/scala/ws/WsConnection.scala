package io.blindnet.dataaccess
package ws

import ws.*

import cats.effect.*
import cats.effect.std.Queue
import io.circe.*
import io.circe.syntax.*

case class WsConnection(queue: Queue[IO, String]) {
  def send[T <: WsPacket](payload: T)(implicit enc: Encoder[T]): IO[Unit] =
    queue.offer(WsPayload(payload).asJson.noSpaces)
}
