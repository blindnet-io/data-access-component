package io.blindnet.dataaccess
package services

import errors.*
import models.*
import endpoints.objects.*
import redis.*
import ws.packets.out.*

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*
import org.http4s.Uri

class RequestService(connectorService: ConnectorService, queryRepository: DataRequestRepository) {
  def get(q: DataRequestPayload): IO[Unit] =
    for {
      callback <- Uri.fromString(q.callback).toOption.orBadRequest("Invalid callback")
      _ <- queryRepository.get(q.request_id).thenBadRequest("Request already exists")
      _ <- queryRepository.set(DataRequest(q.request_id, DataRequestActions.Get, callback))
      conn <- connectorService.connection
      _ <- conn.send(OutPacketDataRequest(q, DataRequestActions.Get))
    } yield ()
}
