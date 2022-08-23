package io.blindnet.dataaccess
package services

import azure.AzureStorage
import errors.*
import models.*
import redis.*
import ws.packets.out.*

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*
import org.http4s.Uri

class DataService(repos: Repositories) {
  def get(requestId: String, dataId: String): IO[Stream[IO, Byte]] =
    IO(AzureStorage.download(dataId))
}
