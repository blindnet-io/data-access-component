package io.blindnet.dataaccess
package services

import azure.AzureStorage
import endpoints.objects.DataCallbackPayload
import errors.*
import models.DataRequestAction
import models.DataRequestReply
import models.Connector
import ws.{WsConnTracker, WsConnection}
import ws.packets.out.OutPacketWelcome

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*
import org.http4s.*
import org.http4s.blaze.client.*

import java.nio.ByteBuffer
import java.util.UUID

class ConnectorService(repos: Repositories, state: Ref[IO, Map[UUID, WsConnTracker]]) {
  implicit val uuidGen: UUIDGen[IO] = UUIDGen.fromSync

  def ws(co: Connector)(x: Unit): IO[Pipe[IO, String, String]] =
    for {
      queue <- Queue.unbounded[IO, String]
      conn = WsConnection(repos, co, queue)
      _ <- conn.send(OutPacketWelcome(co.appId, co.id, co.name))
      _ <- addConnection(co, conn)
    } yield (in: Stream[IO, String]) => {
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
          .evalTap(_.map(conn.receive).getOrElse(removeConnection(co, conn)))
          .unNoneTerminate
          .drain
      )
    }

  def sendMainData(co: Connector)(requestId: String, last: Boolean, data: Stream[IO, Byte]): IO[Unit] =
    for {
      request <- repos.dataRequests.get(co.appId, requestId).orBadRequest("Request not found")
        .flatMap(req => req.dataIds.get(co.id) match
          case Some(value) => IO.pure(req)
          case None => for {
            _ <- req.connectors.contains(co.id).orBadRequest("Request does not contain this connector")
            _ <- (req.action == DataRequestAction.GET).orBadRequest("Request is not GET")
            dataId <- UUIDGen.randomString
            newReq = req.withReply(co, DataRequestReply.ACCEPT).withDataId(co, dataId)
            _ <- AzureStorage.createAppendBlob(newReq.dataPath(dataId))
            _ <- repos.dataRequests.set(newReq)
          } yield newReq)
      _ <- data.through(AzureStorage.append(request.dataPath(request.dataIds(co.id)))).compile.drain
      _ <- request.tryCallback(repos, co, last)
    } yield ()

  def sendAdditionalData(co: Connector)(requestId: String, data: Stream[IO, Byte]): IO[String] =
    for {
      request <- repos.dataRequests.get(co.appId, requestId).orBadRequest("Request not found")
      _ <- request.connectors.contains(co.id).orBadRequest("Request does not contain this connector")
      dataId <- UUIDGen.randomString
      _ <- AzureStorage.createAppendBlob(request.dataPath(dataId))
      _ <- repos.dataRequests.set(request.withAdditionalDataId(co, dataId))
      _ <- data.through(AzureStorage.append(request.dataPath(dataId))).compile.drain
    } yield request.dataUrl(dataId)

  private def tracker(co: Connector): IO[WsConnTracker] =
    for {
      existing <- state.get.map(_.get(co.id))
      tracker <- existing match
        case Some(value) => IO.pure(value)
        case None => {
          val newTracker = WsConnTracker()
          state.update(_ + (co.id -> newTracker)).as(newTracker)
        }
    } yield tracker

  private def updateTracker(co: Connector, f: WsConnTracker => WsConnTracker): IO[Unit] =
    state.update(map => map + (co.id -> f(map.getOrElse(co.id, WsConnTracker()))))

  private def addConnection(co: Connector, conn: WsConnection): IO[Unit] =
    updateTracker(co, _.add(conn))

  private def removeConnection(co: Connector, conn: WsConnection): IO[Unit] =
    updateTracker(co, _.remove(conn))

  def connection(co: Connector): IO[WsConnection] =
    tracker(co).map(_.get.get)
}

object ConnectorService {
  def apply(repos: Repositories): IO[ConnectorService] =
    Ref[IO].of[Map[UUID, WsConnTracker]](Map.empty).map(new ConnectorService(repos, _))
}
