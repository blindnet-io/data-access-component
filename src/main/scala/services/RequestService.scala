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

import java.util.UUID

class RequestService(connectorService: ConnectorService, repos: Repositories) {
  def create(q: DataRequestPayload): IO[Unit] =
    for {
      app <- repos.apps.findById(UUID.fromString("6f083c15-4ada-4671-a6d1-c671bc9105dc")).orNotFound

      callback <- Uri.fromString(q.callback).toOption.orBadRequest("Invalid callback")
      _ <- repos.dataRequests.get(app.id, q.request_id).thenBadRequest("Request already exists")

      namespaces <- repos.namespaces.findAllByApp(app.id)
      _ <- repos.dataRequests.set(DataRequest(app.id, q.request_id, q.action, namespaces.map(_.id), callback))

      connections <- namespaces.traverse(connectorService.connection)
      _ <- connections.traverse(_.send(OutPacketDataRequest(q)))
    } yield ()
}
