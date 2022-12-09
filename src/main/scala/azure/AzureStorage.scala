package io.blindnet.dataaccess
package azure

import cats.data.OptionT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.azure.storage.blob.BlobClientBuilder
import com.azure.storage.blob.models.{AppendBlobRequestConditions, BlobErrorCode, BlobStorageException}
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

  private def getBlobInputStream(blobId: String): IO[Option[BlobInputStream]] =
    IO.blocking(Some(buildBlobClient(blobId).openInputStream()))
      .handleErrorWith {
        case e: BlobStorageException =>
          if e.getErrorCode == BlobErrorCode.BLOB_NOT_FOUND then IO.pure(None)
          else IO.raiseError(e)
        case e => IO.raiseError(e)
      }

  def createAppendBlob(blobId: String): IO[Unit] =
    IO(buildBlobClient(blobId).getAppendBlobClient.create())

  def append(blobId: String): Pipe[IO, Byte, Nothing] =
    writeOutputStream(getBlobAppendOutputStream(blobId))

  def download(blobId: String): IO[Option[Stream[IO, Byte]]] =
    OptionT(getBlobInputStream(blobId))
      .map(is => readInputStream(IO.pure(is), 1000))
      .value
}
