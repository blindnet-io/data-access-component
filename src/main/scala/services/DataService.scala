package io.blindnet.dataaccess
package services

import azure.AzureStorage
import errors.*
import models.*
import objects.*
import redis.*
import ws.packets.out.*

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*
import org.http4s.Uri

class DataService(queryRepository: QueryRepository) {
  def get(requestId: String, dataId: String): IO[Stream[IO, Byte]] =
    for {
      request <- queryRepository.get(requestId).orBadRequest("Request not found")
      _ <- request.dataPath.contains(dataId).orBadRequest("Data not found")
    } yield AzureStorage.download(dataId)
}
