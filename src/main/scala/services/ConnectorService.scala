package io.blindnet.dataaccess
package services

import azure.AzureStorage
import endpoints.objects.DataCallbackPayload
import errors.*
import models.DataRequestAction
import models.DataRequestReply
import ws.{WsConnection, WsConnTracker}

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*
import org.http4s.*
import org.http4s.blaze.client.*

import java.nio.ByteBuffer

class ConnectorService(repos: Repositories, state: Ref[IO, Map[String, WsConnTracker]]) {
  implicit val uuidGen: UUIDGen[IO] = UUIDGen.fromSync

  def ws(appId: String): IO[Pipe[IO, String, String]] =
    for {
      queue <- Queue.unbounded[IO, String]
      conn = WsConnection(repos, appId, queue)
      _ <- addConnection(appId, conn)
    } yield (in: Stream[IO, String]) => {
      Stream.fromQueueUnterminated(queue, Int.MaxValue)
        .mergeHaltBoth(in.evalTap(conn.receive).drain)
    }

  def sendMainData(appId: String, requestId: String, last: Boolean, data: Stream[IO, Byte]): IO[Unit] =
    for {
      request <- repos.dataRequests.get(appId, requestId).orBadRequest("Request not found")
        .flatMap(req => req.dataId match
          case Some(value) => IO.pure(req)
          case None => for {
            _ <- (req.action == DataRequestAction.GET).orBadRequest("Request is not GET")
            dataId <- UUIDGen.randomString
            newReq = req.copy(reply = Some(DataRequestReply.ACCEPT), dataId = Some(dataId))
            _ <- AzureStorage.createAppendBlob(newReq.dataPath(dataId))
            _ <- repos.dataRequests.set(newReq)
          } yield newReq)
      _ <- data.through(AzureStorage.append(request.dataPath(request.dataId.get))).compile.drain
      _ <- request.tryCallback(repos, last)
    } yield ()

  def sendAdditionalData(appId: String, requestId: String, data: Stream[IO, Byte]): IO[String] =
    for {
      request <- repos.dataRequests.get(appId, requestId).orBadRequest("Request not found")
      dataId <- UUIDGen.randomString
      _ <- AzureStorage.createAppendBlob(request.dataPath(dataId))
      _ <- repos.dataRequests.set(request.copy(additionalDataIds = dataId :: request.additionalDataIds))
      _ <- data.through(AzureStorage.append(request.dataPath(dataId))).compile.drain
    } yield request.dataUrl(dataId)

  private def tracker(appId: String): IO[WsConnTracker] =
    for {
      existing <- state.get.map(_.get(appId))
      tracker <- existing match
        case Some(value) => IO.pure(value)
        case None => {
          val newTracker = WsConnTracker()
          state.update(_ + (appId -> newTracker)).as(newTracker)
        }
    } yield tracker

  private def addConnection(appId: String, conn: WsConnection): IO[Unit] =
    tracker(appId).map(_.add(conn)).flatMap(newTracker => state.update(_ + (appId -> newTracker)))

  def connection(appId: String): IO[WsConnection] =
    tracker(appId).map(_.get.get)
}

object ConnectorService {
  def apply(repos: Repositories): IO[ConnectorService] =
    Ref[IO].of[Map[String, WsConnTracker]](Map.empty).map(new ConnectorService(repos, _))
}
