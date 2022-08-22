package io.blindnet.dataaccess
package services

import azure.AzureStorage
import endpoints.objects.DataCallbackPayload
import errors.*
import models.DataRequestReply
import ws.WsConnection

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*
import org.http4s.*
import org.http4s.blaze.client.*
import org.http4s.circe.CirceEntityEncoder.*

import java.nio.ByteBuffer

class ConnectorService(repos: Repositories, connectionRef: Ref[IO, Option[WsConnection]]) {
  implicit val uuidGen: UUIDGen[IO] = UUIDGen.fromSync

  def ws(x: Unit): IO[Pipe[IO, String, String]] =
    for {
      queue <- Queue.unbounded[IO, String]
      conn = WsConnection(repos, queue)
      _ <- connectionRef.update(_ => Some(conn))
    } yield (in: Stream[IO, String]) => {
      Stream.fromQueueUnterminated(queue, Int.MaxValue)
        .mergeHaltBoth(in.evalTap(conn.receive).drain)
    }

  def sendMainData(requestId: String, last: Boolean, data: Stream[IO, Byte]): IO[Unit] =
    for {
      request <- repos.dataRequests.get(requestId).orBadRequest("Request not found")
      dataId <- request.dataId.map(IO.pure).getOrElse(for {
        dataId <- UUIDGen.randomString
        _ <- AzureStorage.createAppendBlob(dataId)
        _ <- repos.dataRequests.set(request.copy(reply = Some(DataRequestReply.ACCEPT), dataId = Some(dataId)))
      } yield dataId)
      _ <- data.through(AzureStorage.append(dataId)).compile.drain
      _ <- if last then BlazeClientBuilder[IO].resource.use(_.successful(Request[IO](
        Method.POST,
        request.callback,
      ).withEntity(DataCallbackPayload(request.id, true, Some(request.dataUrl(dataId))))))
      else IO.println("some other data should follow")
    } yield ()

  def sendAdditionalData(requestId: String, data: Stream[IO, Byte]): IO[String] =
    for {
      request <- repos.dataRequests.get(requestId).orBadRequest("Request not found")
      dataId <- UUIDGen.randomString
      _ <- AzureStorage.createAppendBlob(dataId)
      _ <- repos.dataRequests.set(request.copy(additionalDataIds = dataId :: request.additionalDataIds))
      _ <- data.through(AzureStorage.append(dataId)).compile.drain
    } yield request.dataUrl(dataId)

  def connection: IO[WsConnection] =
    connectionRef.get.map(_.get)
}

object ConnectorService {
  def apply(repos: Repositories): IO[ConnectorService] =
    Ref[IO].of[Option[WsConnection]](None).map(ref => new ConnectorService(repos, ref))
}
