package io.blindnet.dataaccess
package redis

import models.DataRequest

import cats.effect.*
import dev.profunktor.redis4cats.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.http4s.circe.*

class DataRequestRepository(redis: RedisCommands[IO, String, String]) {
  def get(id: String): IO[Option[DataRequest]] =
    redis.hGet("data_requests", id).map(parseJson)

  def set(query: DataRequest): IO[Unit] =
    redis.hSet("data_requests", query.id, query.asJson.noSpaces).void

  private def parseJson(json: String): DataRequest =
    parse(json).toOption.get.as[DataRequest].toOption.get

  private def parseJson(opt: Option[String]): Option[DataRequest] =
    opt.map(parseJson)
}
