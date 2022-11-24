package io.blindnet.dataaccess
package db

import models.{Connector, CustomConnector}

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import io.blindnet.identityclient.auth.StRepository

import java.util.UUID

class ConnectorRepository(xa: Transactor[IO]) extends StRepository[CustomConnector, IO] {
  def findById(appId: UUID, id: UUID): IO[Option[Connector]] =
    sql"select id, app_id, name, type, config, token from connectors where app_id=$appId and id=$id"
      .query[Connector].option.transact(xa)
    
  override def findByToken(token: String): IO[Option[CustomConnector]] =
    sql"select id, app_id, name, type, config, token from connectors where token=$token"
      .query[CustomConnector].option.transact(xa)

  def findAllByApp(appId: UUID): IO[List[Connector]] =
    sql"select id, app_id, name, type, config, token from connectors where app_id=$appId"
      .query[Connector].to[List].transact(xa)

  def insert(co: Connector): IO[Unit] =
    sql"""insert into connectors (id, app_id, name, type, config, token) values $co"""
      .update.run.transact(xa).void

  def updateToken(appId: UUID, id: UUID, token: String): IO[Unit] =
    sql"update connectors set token=$token where app_id=$appId and id=$id"
      .update.run.transact(xa).void
}
