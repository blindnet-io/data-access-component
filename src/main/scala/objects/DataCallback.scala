package io.blindnet.dataaccess
package objects

import io.circe.*
import io.circe.generic.semiauto.*

case class DataCallback(
  request_id: String,
  accepted: Boolean,
  data_url: Option[String],
)

object DataCallback {
  implicit val encoder: Encoder[DataCallback] = deriveEncoder[DataCallback]
}
