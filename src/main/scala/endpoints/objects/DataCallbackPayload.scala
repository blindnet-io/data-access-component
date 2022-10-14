package io.blindnet.dataaccess
package endpoints.objects

import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class DataCallbackPayload(
  app_id: UUID,
  request_id: String,
  namespace: NamespacePayload,
  accepted: Boolean,
  data_url: Option[String] = None,
)

object DataCallbackPayload {
  implicit val encoder: Encoder[DataCallbackPayload] = deriveEncoder[DataCallbackPayload]
}

case class NamespacePayload(
  id: UUID,
  name: String,
)

object NamespacePayload {
  given Encoder[NamespacePayload] = deriveEncoder[NamespacePayload]
}
