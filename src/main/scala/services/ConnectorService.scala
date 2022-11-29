package io.blindnet.dataaccess
package services

import azure.AzureStorage
import endpoints.auth.ConnectorAuthenticator
import endpoints.objects.DataCallbackPayload
import errors.*
import models.{Connector, CustomConnector, DataRequestAction, DataRequestReply}
import ws.packets.out.OutPacketWelcome
import ws.*

import cats.data.EitherT
import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*
import jdk.jshell.spi.ExecutionControl.NotImplementedException
import org.http4s.*
import org.http4s.blaze.client.*
import sttp.model.StatusCode

import java.nio.ByteBuffer
import java.util.UUID
import scala.util.Try

case class ConnectorService(repos: Repositories, state: WsState) {
  implicit val uuidGen: UUIDGen[IO] = UUIDGen.fromSync

  def dualAuth(authenticator: ConnectorAuthenticator)
              (authOpt: Option[String], appOpt: Option[String], coOpt: Option[String]): IO[Either[String, Connector]] =
    if authOpt.contains("Bearer " + Env.get.globalConnectorToken) then
      (for {
        appId <- EitherT.fromOption[IO](appOpt, "Missing X-Application-ID header")
          .flatMap(raw => EitherT.fromOption[IO](Try(UUID.fromString(raw)).toOption, "Invalid application ID"))
        coId <- EitherT.fromOption[IO](coOpt, "Missing X-Connector-ID header")
          .flatMap(raw => EitherT.fromOption[IO](Try(UUID.fromString(raw)).toOption, "Invalid connector ID"))
        co <- EitherT.fromOptionF(repos.connectors.findById(appId, coId), "Connector not found")
      } yield co).value
    else authenticator.authenticateHeader(authOpt)

  def wsCustom(co: CustomConnector)(x: Unit): IO[Pipe[IO, String, String]] =
    for {
      conn <- CustomWsConnection(repos, co)
      _ <- conn.send(OutPacketWelcome(co.appId, co.id, co.name))
      _ <- state.addCustomConnection(co, conn)
    } yield conn.pipe(state.removeCustomConnection(co, conn))

  def wsGlobal(x: Unit)(typesHeader: String): IO[Pipe[IO, String, String]] =
    val types = typesHeader.split(',').toList.distinct
    for {
      _ <- repos.connectors.countTypesByIds(types)
        .map(_ == types.length).flatMap(_.orBadRequest("Unknown connector type"))
      conn <- GlobalWsConnection(repos, types)
      _ <- conn.send(OutPacketWelcome())
      _ <- state.addGlobalConnection(conn)
    } yield conn.pipe(state.removeGlobalConnection(conn))

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
}

object ConnectorService {
  def apply(repos: Repositories): IO[ConnectorService] =
    WsState().map(new ConnectorService(repos, _))
}
