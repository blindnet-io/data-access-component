package io.blindnet.dataaccess
package azure

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.azure.storage.blob.BlobClientBuilder
import com.azure.storage.blob.models.AppendBlobRequestConditions
import com.azure.storage.blob.specialized.{BlobInputStream, BlobOutputStream}
import com.azure.storage.common.StorageSharedKeyCredential
import fs2.*
import fs2.io.*

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}
import scala.jdk.CollectionConverters.*

object AzureStorage {
  val version = "2021-04-10"

  private val accountName = Env.get.azureStorageAccountName
  private val accountKey = Env.get.azureStorageAccountKey
  private val containerName = Env.get.azureStorageContainerName

  private val credential = StorageSharedKeyCredential(accountName, accountKey)

  private def buildBlobClient(blobId: String) =
    BlobClientBuilder()
      .endpoint(s"https://$accountName.blob.core.windows.net/$containerName/$blobId")
      .credential(credential)
      .buildClient()

  private def getBlobAppendOutputStream(blobId: String): IO[BlobOutputStream] =
    IO(buildBlobClient(blobId).getAppendBlobClient.getBlobOutputStream)

  private def getBlobInputStream(blobId: String): IO[BlobInputStream] =
    IO(buildBlobClient(blobId).openInputStream())

  def createAppendBlob(blobId: String): IO[Unit] =
    IO(buildBlobClient(blobId).getAppendBlobClient.create())

  def append(blobId: String): Pipe[IO, Byte, Nothing] =
    writeOutputStream(getBlobAppendOutputStream(blobId))

  def download(blobId: String): Stream[IO, Byte] =
    readInputStream(getBlobInputStream(blobId), 1000)
}
