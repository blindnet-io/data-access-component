package io.blindnet.dataaccess
package ws.packets.in

import azure.AzureStorage
import endpoints.objects.DataCallbackPayload
import errors.*
import ws.{WsConnection, WsInPacket}

import cats.effect.IO
import cats.effect.std.UUIDGen
import io.blindnet.dataaccess.models.DataRequestReplies
import io.circe.*
import io.circe.generic.semiauto.*
import org.http4s.*
import org.http4s.blaze.client.*
import org.http4s.circe.CirceEntityEncoder.*

import java.nio.ByteBuffer

case class InPacketDataRequestReply(request_id: String, typ: DataRequestReplies.DataRequestReply) extends WsInPacket {
  implicit val uuidGen: UUIDGen[IO] = UUIDGen.fromSync

  override def handle(conn: WsConnection, remaining: ByteBuffer): IO[Unit] = for {
    _ <- IO.println("got reply! " + typ)
    query <- conn.queryRepo.get(request_id).orNotFound.flatMap(q =>
      if typ == DataRequestReplies.Accept then for {
        path <- UUIDGen.randomString
        _ <- AzureStorage.createAppendBlob(path)
      } yield q.copy(reply = Some(typ), dataId = Some(path))
      else for {
        _ <- BlazeClientBuilder[IO].resource.use(_.successful(Request[IO](
            Method.POST,
            q.callback,
          ).withEntity(DataCallbackPayload(q.id, false))))
      } yield q.copy(reply = Some(typ)))
    _ <- conn.queryRepo.set(query)
    _ <- IO.println(query)
  } yield ()
}

object InPacketDataRequestReply {
  implicit val decoder: Decoder[InPacketDataRequestReply] =
    Decoder.forProduct2("request_id", "type")(InPacketDataRequestReply.apply)
}
