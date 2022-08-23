package io.blindnet.dataaccess
package services

import azure.AzureStorage
import endpoints.objects.DataCallbackPayload
import errors.*
import models.DataRequestAction
import models.DataRequestReply
import ws.WsConnection

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*
import org.http4s.*
import org.http4s.blaze.client.*

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
        .flatMap(req => req.dataId match
          case Some(value) => IO.pure(req)
          case None => for {
            _ <- (req.action == DataRequestAction.GET).orBadRequest("Request is not GET")
            dataId <- UUIDGen.randomString
            newReq = req.copy(reply = Some(DataRequestReply.ACCEPT), dataId = Some(dataId))
            _ <- AzureStorage.createAppendBlob(dataId)
            _ <- repos.dataRequests.set(newReq)
          } yield newReq)
      _ <- data.through(AzureStorage.append(request.dataId.get)).compile.drain
      _ <- request.tryCallback(repos, last)
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
