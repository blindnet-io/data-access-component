package io.blindnet.dataaccess
package ws

import models.{Connector, CustomConnector, GlobalConnector}
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

sealed trait WsConnection {
  def repos: Repositories

  protected def queue: Queue[IO, String]
  protected def receive(raw: String): IO[Unit]
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

  def send[T <: WsOutPacket](packet: T)(implicit enc: Encoder[T]): IO[Unit] =
    queue.offer(WsOutPayload(packet).asJson.noSpaces)

  protected def parsePacket[T <: WsInPacket](raw: String): Option[(T, Option[(UUID, UUID)])] =
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
}

case class CustomWsConnection(repos: Repositories, connector: CustomConnector, queue: Queue[IO, String]) extends WsConnection {
  override protected def receive(raw: String): IO[Unit] =
    parsePacket[WsInPacket](raw) match
      case Some((packet, _)) => packet.handle(this, connector)
      case None => IO.println("ignoring invalid WS packet")
}

object CustomWsConnection {
  def apply(repos: Repositories, connector: CustomConnector): IO[CustomWsConnection] =
    Queue.unbounded[IO, String].map(new CustomWsConnection(repos, connector, _))
}

case class GlobalWsConnection(repos: Repositories, types: List[String], queue: Queue[IO, String]) extends WsConnection {
  override protected def receive(raw: String): IO[Unit] =
    parsePacket[WsInPacket](raw).flatMap((p, opt) => opt.map((p, _))) match
      case Some((packet, (appId, coId))) => repos.connectors.findById(appId, coId).flatMap(_ match
        case Some(co) => packet.handle(this, co)
        case None => IO.println("ignoring invalid WS packet - connector not found")
      )
      case None => IO.println("ignoring invalid WS packet")

  def send[T <: WsOutPacket](connector: GlobalConnector, packet: T)(implicit enc: Encoder[T]): IO[Unit] =
    queue.offer(WsOutGlobalPayload(connector.appId, connector.id, connector.name, connector.typ, connector.config, packet).asJson.noSpaces)
}

object GlobalWsConnection {
  def apply(repos: Repositories, types: List[String]): IO[GlobalWsConnection] =
    Queue.unbounded[IO, String].map(new GlobalWsConnection(repos, types, _))
}
