package io.blindnet.dataaccess
package models

import endpoints.objects.DataCallbackPayload

import cats.effect.*
import io.circe.*
import io.circe.generic.semiauto.*
import org.http4s.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*

import java.util.UUID

case class DataRequest(
  appId: UUID,
  id: String,
  action: DataRequestAction,
  callback: Uri,
  reply: Option[DataRequestReply] = None,
  dataId: Option[String] = None,
  additionalDataIds: List[String] = Nil,
) {
  def dataPath(dataId: String) = s"$appId/$id/$dataId"
  def dataUrl(dataId: String) = s"${Env.get.baseUrl}/v1/data/${dataPath(dataId)}"

  def hasCompleteReply(mainDataSent: Boolean): Boolean =
    reply.isDefined && (action == DataRequestAction.DELETE ||
      (action == DataRequestAction.GET && dataId.isDefined && mainDataSent)
    )

  /**
   * Tries calling the callback if a complete reply has been received, else does nothing.
   * @param mainDataSent if this is a GET request, whether the main data has been sent
   */
  def tryCallback(repos: Repositories, mainDataSent: Boolean = false): IO[Unit] =
    if hasCompleteReply(mainDataSent)
    then for {
      _ <- BlazeClientBuilder[IO].resource.use(_.successful(Request[IO](
        Method.POST,
        callback,
      ).withEntity(DataCallbackPayload(appId, id, reply.get == DataRequestReply.ACCEPT, dataId.map(dataUrl)))))
      _ <- repos.dataRequests.delete(appId, id)
    } yield ()
    else IO.unit
}

object DataRequest {
  implicit val encoder: Encoder[DataRequest] = deriveEncoder[DataRequest]
  implicit val decoder: Decoder[DataRequest] = deriveDecoder[DataRequest]
}
