package io.blindnet.dataaccess
package db

import models.App

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import io.blindnet.identityclient.auth.StRepository

class AppRepository(xa: Transactor[IO]) extends StRepository[App, IO] {
  override def findByToken(token: String): IO[Option[App]] =
    sql"select id, token from apps where token=$token"
      .query[App].option.transact(xa)
}
