package io.blindnet.dataaccess
package services

import azure.AzureStorage
import endpoints.objects.DataCallbackPayload
import errors.*
import models.DataRequestAction
import models.DataRequestReply
import models.Namespace
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

  def ws(ns: Namespace)(x: Unit): IO[Pipe[IO, String, String]] =
    for {
      queue <- Queue.unbounded[IO, String]
      conn = WsConnection(repos, ns, queue)
      _ <- conn.send(OutPacketWelcome(ns.appId, ns.id, ns.name))
      _ <- addConnection(ns, conn)
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
          .evalTap(_.map(conn.receive).getOrElse(removeConnection(ns, conn)))
          .unNoneTerminate
          .drain
      )
    }

  def sendMainData(ns: Namespace)(requestId: String, last: Boolean, data: Stream[IO, Byte]): IO[Unit] =
    for {
      request <- repos.dataRequests.get(ns.appId, requestId).orBadRequest("Request not found")
        .flatMap(req => req.dataIds.get(ns.id) match
          case Some(value) => IO.pure(req)
          case None => for {
            _ <- req.namespaces.contains(ns.id).orBadRequest("Request does not contain this namespace")
            _ <- (req.action == DataRequestAction.GET).orBadRequest("Request is not GET")
            dataId <- UUIDGen.randomString
            newReq = req.withReply(ns, DataRequestReply.ACCEPT).withDataId(ns, dataId)
            _ <- AzureStorage.createAppendBlob(newReq.dataPath(dataId))
            _ <- repos.dataRequests.set(newReq)
          } yield newReq)
      _ <- data.through(AzureStorage.append(request.dataPath(request.dataIds(ns.id)))).compile.drain
      _ <- request.tryCallback(repos, ns, last)
    } yield ()

  def sendAdditionalData(ns: Namespace)(requestId: String, data: Stream[IO, Byte]): IO[String] =
    for {
      request <- repos.dataRequests.get(ns.appId, requestId).orBadRequest("Request not found")
      _ <- request.namespaces.contains(ns.id).orBadRequest("Request does not contain this namespace")
      dataId <- UUIDGen.randomString
      _ <- AzureStorage.createAppendBlob(request.dataPath(dataId))
      _ <- repos.dataRequests.set(request.withAdditionalDataId(ns, dataId))
      _ <- data.through(AzureStorage.append(request.dataPath(dataId))).compile.drain
    } yield request.dataUrl(dataId)

  private def tracker(ns: Namespace): IO[WsConnTracker] =
    for {
      existing <- state.get.map(_.get(ns.id))
      tracker <- existing match
        case Some(value) => IO.pure(value)
        case None => {
          val newTracker = WsConnTracker()
          state.update(_ + (ns.id -> newTracker)).as(newTracker)
        }
    } yield tracker

  private def updateTracker(ns: Namespace, f: WsConnTracker => WsConnTracker): IO[Unit] =
    state.update(map => map + (ns.id -> f(map.getOrElse(ns.id, WsConnTracker()))))

  private def addConnection(ns: Namespace, conn: WsConnection): IO[Unit] =
    updateTracker(ns, _.add(conn))

  private def removeConnection(ns: Namespace, conn: WsConnection): IO[Unit] =
    updateTracker(ns, _.remove(conn))

  def connection(ns: Namespace): IO[WsConnection] =
    tracker(ns).map(_.get.get)
}

object ConnectorService {
  def apply(repos: Repositories): IO[ConnectorService] =
    Ref[IO].of[Map[UUID, WsConnTracker]](Map.empty).map(new ConnectorService(repos, _))
}
