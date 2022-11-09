package io.blindnet.dataaccess
package models

import endpoints.objects.{DataCallbackPayload, LightConnectorPayload}
import models.Connector

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
  connectors: List[UUID],
  callback: Uri,
  replies: Map[UUID, DataRequestReply] = Map.empty,
  dataIds: Map[UUID, String] = Map.empty,
  additionalDataIds: Map[UUID, List[String]] = Map.empty,
) {
  def withReply(connector: Connector, reply: DataRequestReply): DataRequest =
    copy(replies = replies + (connector.id -> reply))

  def withDataId(connector: Connector, dataId: String): DataRequest =
    copy(dataIds = dataIds + (connector.id -> dataId))

  def withAdditionalDataId(connector: Connector, dataId: String): DataRequest =
    copy(additionalDataIds = additionalDataIds.updatedWith(connector.id)(opt => Some(dataId :: opt.getOrElse(Nil))))

  def dataPath(dataId: String) = s"$appId/$id/$dataId"
  def dataUrl(dataId: String) = s"${Env.get.baseUrl}/v1/data/${dataPath(dataId)}"

  def hasCompleteReply(connector: Connector, mainDataSent: Boolean): Boolean =
    replies.contains(connector.id) && (
      replies(connector.id) == DataRequestReply.DENY ||
      action == DataRequestAction.DELETE || (
        action == DataRequestAction.GET &&
        dataIds.contains(connector.id) &&
        mainDataSent
      )
    )

  /**
   * Tries calling the callback if a complete reply has been received, else does nothing.
   * @param mainDataSent if this is a GET request, whether the main data has been sent
   */
  def tryCallback(repos: Repositories, connector: Connector, mainDataSent: Boolean = false): IO[Unit] =
    if hasCompleteReply(connector, mainDataSent)
    then for {
      _ <- BlazeClientBuilder[IO].resource.use(_.successful(Request[IO](
        Method.POST,
        callback,
      ).withEntity(DataCallbackPayload(appId, id, LightConnectorPayload(connector.id, connector.name, connector.typ), replies(connector.id) == DataRequestReply.ACCEPT, dataIds.get(connector.id).map(dataUrl)))))
      _ <- repos.dataRequests.delete(appId, id)
    } yield ()
    else IO.unit
}

object DataRequest {
  implicit val encoder: Encoder[DataRequest] = deriveEncoder[DataRequest]
  implicit val decoder: Decoder[DataRequest] = deriveDecoder[DataRequest]
}
