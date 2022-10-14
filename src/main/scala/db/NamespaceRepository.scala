package io.blindnet.dataaccess
package db

import models.Namespace

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import io.blindnet.identityclient.auth.StRepository

import java.util.UUID

class NamespaceRepository(xa: Transactor[IO]) extends StRepository[Namespace, IO] {
  def findById(appId: UUID, id: UUID): IO[Option[Namespace]] =
    sql"select id, app_id, name, token from namespaces where app_id=$appId and id=$id"
      .query[Namespace].option.transact(xa)
    
  override def findByToken(token: String): IO[Option[Namespace]] =
    sql"select id, app_id, name, token from namespaces where token=$token"
      .query[Namespace].option.transact(xa)

  def findAllByApp(appId: UUID): IO[List[Namespace]] =
    sql"select id, app_id, name, token from namespaces where app_id=$appId"
      .query[Namespace].to[List].transact(xa)

  def insert(ns: Namespace): IO[Unit] =
    sql"insert into namespaces (id, app_id, name, token) values (${ns.id}, ${ns.appId}, ${ns.name}, ${ns.token})"
      .update.run.transact(xa).void

  def updateToken(appId: UUID, id: UUID, token: String): IO[Unit] =
    sql"update namespaces set token=$token where app_id=$appId and id=$id"
      .update.run.transact(xa).void
}
