package io.blindnet.dataaccess
package ws.packets.in

import azure.AzureStorage
import endpoints.objects.DataCallbackPayload
import errors.*
import models.{Connector, DataRequestReply}
import ws.{WsConnection, WsInPacket}

import cats.effect.IO
import cats.effect.std.UUIDGen
import io.circe.*
import io.circe.generic.semiauto.*
import org.http4s.*
import org.http4s.blaze.client.*
import org.http4s.circe.CirceEntityEncoder.*

import java.nio.ByteBuffer

case class InPacketDataRequestReply(request_id: String, typ: DataRequestReply) extends WsInPacket {
  override def handle(conn: WsConnection, co: Connector): IO[Unit] = for {
    request <- conn.repos.dataRequests.get(co.appId, request_id).orNotFound
      .flatTap(_.connectors.contains(co.id).orBadRequest("Request does not contain this connector"))
      .map(_.withReply(co, typ))
    _ <- conn.repos.dataRequests.set(request)
    _ <- request.tryCallback(conn.repos, co)
  } yield ()
}

object InPacketDataRequestReply {
  implicit val decoder: Decoder[InPacketDataRequestReply] =
    Decoder.forProduct2("request_id", "type")(InPacketDataRequestReply.apply)
}
