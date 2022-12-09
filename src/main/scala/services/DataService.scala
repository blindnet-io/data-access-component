package io.blindnet.dataaccess
package services

import azure.AzureStorage
import errors.*
import models.*
import redis.*
import ws.packets.out.*

import cats.data.OptionT
import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*
import org.http4s.Uri

import java.util.UUID
import scala.util.matching.Regex

class DataService(repos: Repositories) {
  val dataIdPattern: Regex = "\\.\\w+$".r

  def get(appId: UUID, requestId: String, dataId: String): IO[Stream[IO, Byte]] =
    AzureStorage.download(s"$appId/$requestId/${dataIdPattern.replaceFirstIn(dataId, "")}")
      .flatMap(_.orNotFound)
}
