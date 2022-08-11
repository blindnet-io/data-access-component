package io.blindnet.dataaccess
package models

import io.circe.*
import io.circe.generic.semiauto.*
import org.http4s.circe.*
import org.http4s.Uri

case class DataRequest(
                        id: String,
                        action: DataRequestActions.DataRequestAction,
                        callback: Uri,
                        reply: Option[DataRequestReplies.DataRequestReply] = None,
                        dataId: Option[String] = None,
) {
  implicit val encoder: Encoder[DataRequest] = deriveEncoder[DataRequest]
  implicit val decoder: Decoder[DataRequest] = deriveDecoder[DataRequest]
}
