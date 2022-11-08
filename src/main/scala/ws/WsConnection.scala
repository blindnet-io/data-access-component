package io.blindnet.dataaccess
package ws

import models.Connector
import redis.DataRequestRepository
import ws.*

import cats.data.*
import cats.effect.*
import cats.effect.std.Queue
import fs2.{Pipe, Stream}
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*

import java.nio.ByteBuffer
import java.util.UUID
import scala.util.Try

case class WsConnection(repos: Repositories, connector: Option[Connector], queue: Queue[IO, String]) {
  def pipe(onTerminate: IO[Unit]): Pipe[IO, String, String] = (in: Stream[IO, String]) => {
    // noneTerminate should theoretically be enough to detect disconnects, but the Stream will actually fail
    // with an EOF error and therefore not emit a None.
    // We are keeping the call to noneTerminate, in the unlikely event that it could actually not fail sometimes.
    // unNoneTerminate prevents emitting None twice in this same unlikely event.
    //
    // See (*probably* related):
    // https://github.com/http4s/blaze/issues/668
    // https://github.com/softwaremill/adopt-tapir/pull/223
    Stream.fromQueueUnterminated(queue, Int.MaxValue).merge(
      in.noneTerminate
        .handleErrorWith(_ => Stream(None))
        .evalTap(_.map(receive).getOrElse(onTerminate))
        .unNoneTerminate
        .drain
    )
  }

  private def receive(raw: String): IO[Unit] =
    def parsePacket[T <: WsInPacket](): Option[(T, Option[(UUID, UUID)])] =
      def parseUUID(j: Json): Option[UUID] = j.asString.flatMap(s => Try(UUID.fromString(s)).toOption)

      for {
        payload <- parse(raw).toOption.flatMap(_.asObject)
        typ <- payload("typ").flatMap(_.asString)
        decoder <- WsInPacket.decoders.get(typ)
        packet <- payload("data").flatMap(_.as[T](decoder.asInstanceOf[Decoder[T]]).toOption)
      } yield (packet,
        payload("app_id").flatMap(parseUUID)
          .flatMap(appId => payload("connector_id").flatMap(parseUUID)
            .map((appId, _))))

    for {
      _ <- parsePacket[WsInPacket]() match
        case Some(packet, idOption) =>
          OptionT.fromOption[IO](connector)
            .orElse(OptionT.fromOption[IO](idOption).flatMapF((appId, coId) => repos.connectors.findById(appId, coId)))
            .value.flatMap(packet.handle(this, _))
        case None => IO.println("ignoring invalid WS packet")
    } yield ()

  def send[T <: WsOutPacket](packet: T)(implicit enc: Encoder[T]): IO[Unit] =
    queue.offer(WsOutPayload(packet).asJson.noSpaces)

  def sendGlobal[T <: WsOutPacket](connector: Connector, packet: T)(implicit enc: Encoder[T]): IO[Unit] =
    queue.offer(WsOutGlobalPayload(connector.appId, connector.id, connector.typ.get, connector.config, packet).asJson.noSpaces)
}

object WsConnection {
  def apply(repos: Repositories, connector: Option[Connector]): IO[WsConnection] =
    Queue.unbounded[IO, String].map(new WsConnection(repos, connector, _))
}
