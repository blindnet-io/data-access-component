package io.blindnet.dataaccess
package ws.packets.in

import azure.AzureStorage
import endpoints.objects.DataCallbackPayload
import errors.*
import models.DataRequestReply
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
  override def handle(conn: WsConnection, remaining: ByteBuffer): IO[Unit] = for {
    _ <- IO.println("got reply! " + typ)
    query <- conn.repos.dataRequests.get(request_id).orNotFound.flatMap(q =>
      if typ == DataRequestReply.ACCEPT then IO.pure(q.copy(reply = Some(typ)))
      else for {
        _ <- BlazeClientBuilder[IO].resource.use(_.successful(Request[IO](
            Method.POST,
            q.callback,
          ).withEntity(DataCallbackPayload(q.id, false))))
      } yield q.copy(reply = Some(typ)))
    _ <- conn.repos.dataRequests.set(query)
    _ <- IO.println(query)
  } yield ()
}

object InPacketDataRequestReply {
  implicit val decoder: Decoder[InPacketDataRequestReply] =
    Decoder.forProduct2("request_id", "type")(InPacketDataRequestReply.apply)
}
