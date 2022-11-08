package io.blindnet.dataaccess

import db.*
import redis.*

import cats.effect.*
import dev.profunktor.redis4cats.*
import dev.profunktor.redis4cats.effect.Log.Stdout.*
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

class Repositories(xa: Transactor[IO], redis: RedisCommands[IO, String, String]) {
  val apps: AppRepository = AppRepository(xa)
  val connectors: ConnectorRepository = ConnectorRepository(xa)

  val dataRequests: DataRequestRepository = DataRequestRepository(redis)
}

object Repositories {
  def apply(): Resource[IO, Repositories] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](32)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        Env.get.dbUri,
        Env.get.dbUsername,
        Env.get.dbPassword,
        ec,
      )
      redis <- Redis[IO].utf8("redis://127.0.0.1")
    } yield new Repositories(xa, redis)
}
