package io.blindnet.dataaccess
package endpoints.objects

import io.circe.*
import io.circe.generic.semiauto.*

case class DataCallbackPayload(
  request_id: String,
  accepted: Boolean,
  data_url: Option[String],
)

object DataCallbackPayload {
  implicit val encoder: Encoder[DataCallbackPayload] = deriveEncoder[DataCallbackPayload]
}
