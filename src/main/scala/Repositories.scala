package io.blindnet.dataaccess

import redis.*

import cats.effect.*
import dev.profunktor.redis4cats.*
import dev.profunktor.redis4cats.effect.Log.Stdout.*

class Repositories(redis: RedisCommands[IO, String, String]) {
  val dataRequests: DataRequestRepository = DataRequestRepository(redis)
}

object Repositories {
  def apply(): Resource[IO, Repositories] =
    Redis[IO].utf8("redis://127.0.0.1").map(new Repositories(_))
}
