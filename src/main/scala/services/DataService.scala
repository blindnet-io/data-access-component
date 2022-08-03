package io.blindnet.dataaccess
package services

import objects.*
import ws.packets.*

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*

class DataService(connectorService: ConnectorService) {
  def get(q: DataQuery): IO[Unit] =
    for {
      conn <- connectorService.connection
      _ <- conn.send(DataQueryPacket(q, DataActions.Get))
    } yield ()
}
