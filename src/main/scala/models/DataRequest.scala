package io.blindnet.dataaccess
package models

import endpoints.objects.{DataCallbackPayload, NamespacePayload}
import models.Namespace

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
  namespaces: List[UUID],
  callback: Uri,
  replies: Map[UUID, DataRequestReply] = Map.empty,
  dataIds: Map[UUID, String] = Map.empty,
  additionalDataIds: Map[UUID, List[String]] = Map.empty,
) {
  def withReply(ns: Namespace, reply: DataRequestReply): DataRequest =
    copy(replies = replies + (ns.id -> reply))

  def withDataId(ns: Namespace, dataId: String): DataRequest =
    copy(dataIds = dataIds + (ns.id -> dataId))

  def withAdditionalDataId(ns: Namespace, dataId: String): DataRequest =
    copy(additionalDataIds = additionalDataIds.updatedWith(ns.id)(opt => Some(dataId :: opt.getOrElse(Nil))))

  def dataPath(dataId: String) = s"$appId/$id/$dataId"
  def dataUrl(dataId: String) = s"${Env.get.baseUrl}/v1/data/${dataPath(dataId)}"

  def hasCompleteReply(ns: Namespace, mainDataSent: Boolean): Boolean =
    replies.contains(ns.id) && (action == DataRequestAction.DELETE ||
      (action == DataRequestAction.GET && dataIds.contains(ns.id) && mainDataSent)
    )

  /**
   * Tries calling the callback if a complete reply has been received, else does nothing.
   * @param mainDataSent if this is a GET request, whether the main data has been sent
   */
  def tryCallback(repos: Repositories, ns: Namespace, mainDataSent: Boolean = false): IO[Unit] =
    if hasCompleteReply(ns, mainDataSent)
    then for {
      _ <- BlazeClientBuilder[IO].resource.use(_.successful(Request[IO](
        Method.POST,
        callback,
      ).withEntity(DataCallbackPayload(appId, id, NamespacePayload(ns.id, ns.name), replies(ns.id) == DataRequestReply.ACCEPT, dataIds.get(ns.id).map(dataUrl)))))
      _ <- repos.dataRequests.delete(appId, id)
    } yield ()
    else IO.unit
}

object DataRequest {
  implicit val encoder: Encoder[DataRequest] = deriveEncoder[DataRequest]
  implicit val decoder: Decoder[DataRequest] = deriveDecoder[DataRequest]
}
