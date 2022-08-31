package io.blindnet.dataaccess
package endpoints.objects

import io.circe.*
import io.circe.generic.semiauto.*

case class DataCallbackPayload(
  app_id: String,
  request_id: String,
  accepted: Boolean,
  data_url: Option[String] = None,
)

object DataCallbackPayload {
  implicit val encoder: Encoder[DataCallbackPayload] = deriveEncoder[DataCallbackPayload]
}
