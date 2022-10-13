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

import java.util.UUID

class DataRequestRepository(redis: RedisCommands[IO, String, String]) {
  def get(appId: UUID, id: String): IO[Option[DataRequest]] =
    redis.hGet(s"data_requests:$appId", id).map(parseJson)

  def set(query: DataRequest): IO[Unit] =
    redis.hSet(s"data_requests:${query.appId}", query.id, query.asJson.noSpaces).void

  def delete(appId: UUID, id: String): IO[Unit] =
    redis.hDel(s"data_requests:$appId", id).void

  private def parseJson(json: String): DataRequest =
    parse(json).toOption.get.as[DataRequest].toOption.get

  private def parseJson(opt: Option[String]): Option[DataRequest] =
    opt.map(parseJson)
}
