package io.blindnet.dataaccess
package db

import models.{Connector, GlobalConnector, CustomConnector}

import cats.data.NonEmptyList
import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import io.blindnet.identityclient.auth.StRepository

import java.util.UUID

class ConnectorRepository(xa: Transactor[IO]) extends StRepository[CustomConnector, IO] {
  def countTypesByIds(ids: List[String]): IO[Int] =
    NonEmptyList.fromList(ids) match
      case Some(nel) => (fr"select count(*) from connector_types where" ++ Fragments.in(fr"id", nel))
        .query[Int].unique.transact(xa)
      case None => IO.pure(0)

  def findAllTypes(): IO[List[String]] =
    sql"select id from connector_types"
      .query[String].to[List].transact(xa)

  def findById(appId: UUID, id: UUID): IO[Option[Connector]] =
    sql"select id, app_id, name, type, config, token from connectors where app_id=$appId and id=$id"
      .query[Connector].option.transact(xa)

  override def findByToken(token: String): IO[Option[CustomConnector]] =
    sql"select id, app_id, name, token from connectors where token=$token"
      .query[CustomConnector].option.transact(xa)

  def findAllByApp(appId: UUID): IO[List[Connector]] =
    sql"select id, app_id, name, type, config, token from connectors where app_id=$appId"
      .query[Connector].to[List].transact(xa)

  def insert(co: Connector): IO[Unit] =
    co match {
      case GlobalConnector(id, appId, name, typ, config) => 
        sql"""insert into connectors (id, app_id, name, type, config) values ($id, $appId, $name, $typ, $config)"""
          .update.run.transact(xa).void
      case CustomConnector(id, appId, name, token) =>
        sql"""insert into connectors (id, app_id, name, token) values ($id, $appId, $name, $token)"""
          .update.run.transact(xa).void
    }
    

  def updateToken(appId: UUID, id: UUID, token: String): IO[Unit] =
    sql"update connectors set token=$token where app_id=$appId and id=$id"
      .update.run.transact(xa).void
}
