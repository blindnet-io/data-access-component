package io.blindnet.dataaccess
package ws.packets.in

import azure.AzureStorage
import endpoints.objects.DataCallbackPayload
import errors.*
import ws.{WsConnection, WsInPacket}

import cats.effect.IO
import fs2.Stream
import io.circe.*
import io.circe.generic.semiauto.*
import org.http4s.*
import org.http4s.blaze.client.*
import org.http4s.circe.CirceEntityEncoder.*

import java.nio.ByteBuffer
import java.util.Base64

case class InPacketData(request_id: String, last: Boolean) extends WsInPacket {
  override def handle(conn: WsConnection, remaining: ByteBuffer): IO[Unit] =
    val data = new Array[Byte](remaining.remaining())
    remaining.get(data)

    for {
      _ <- IO.println("got data! len=" + data.length)
      query <- conn.queryRepo.get(request_id).orNotFound
      dataPath <- query.dataId.orBadRequest("request has not been accepted")
      _ <- Stream(data: _*).covary[IO].through(AzureStorage.append(dataPath)).compile.drain
      _ <- IO.println("uploaded block")
      _ <- if last then BlazeClientBuilder[IO].resource.use(_.successful(Request[IO](
          Method.POST,
          query.callback,
        ).withEntity(DataCallbackPayload(query.id, true, Some(s"v1/${Env.get.baseUrl}/data/$request_id/$dataPath")))))
        else IO.println("some other data should follow")
    } yield ()
}

object InPacketData {
  implicit val decoder: Decoder[InPacketData] = deriveDecoder[InPacketData]
}
