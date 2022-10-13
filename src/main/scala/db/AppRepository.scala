package io.blindnet.dataaccess
package db

import models.App

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import io.blindnet.identityclient.auth.StRepository

import java.util.UUID

class AppRepository(xa: Transactor[IO]) extends StRepository[App, IO] {
  def findById(id: UUID): IO[Option[App]] =
    sql"select id, token from apps where id=$id"
      .query[App].option.transact(xa)

  override def findByToken(token: String): IO[Option[App]] =
    sql"select id, token from apps where token=$token"
      .query[App].option.transact(xa)

  def insert(app: App): IO[Unit] =
    sql"insert into apps (id, token) VALUES (${app.id}, ${app.token})"
      .update.run.transact(xa).void
  
  def updateToken(appId: UUID, token: String): IO[Unit] =
    sql"update apps set token=$token where id=$appId"
      .update.run.transact(xa).void
}
