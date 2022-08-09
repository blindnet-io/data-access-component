package io.blindnet.dataaccess
package redis

import models.Query
import objects.*

import cats.effect.*
import dev.profunktor.redis4cats.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

class QueryRepository(redis: RedisCommands[IO, String, String]) {
  def get(id: String): IO[Option[Query]] =
    redis.hGet("queries", id).map(parseJson)

  def set(query: Query): IO[Unit] =
    redis.hSet("queries", query.id, query.asJson.noSpaces).void

  private def parseJson(json: String): Query =
    parse(json).toOption.get.as[Query].toOption.get

  private def parseJson(opt: Option[String]): Option[Query] =
    opt.map(parseJson)
}
