package io.blindnet.dataaccess
package services

import errors.*
import models.*
import endpoints.objects.*
import redis.*
import ws.packets.out.*

import cats.effect.*
import cats.effect.std.*
import cats.implicits.*
import fs2.*
import fs2.concurrent.*
import org.http4s.Uri

class RequestService(connectorService: ConnectorService, repos: Repositories) {
  def create(app: App)(q: DataRequestPayload): IO[Unit] =
    for {
      callback <- Uri.fromString(q.callback).toOption.orBadRequest("Invalid callback")
      _ <- repos.dataRequests.get(app.id, q.request_id).thenBadRequest("Request already exists")

      connectors <- repos.connectors.findAllByApp(app.id)
      _ <- repos.dataRequests.set(DataRequest(app.id, q.request_id, q.action, connectors.map(_.id), callback))

      connections <- connectors.traverse(connectorService.connection)
      _ <- connections.traverse(_.send(OutPacketDataRequest(q)))
    } yield ()
}
