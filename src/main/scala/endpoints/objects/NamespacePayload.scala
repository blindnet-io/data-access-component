package io.blindnet.dataaccess
package endpoints.objects

import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class NamespacePayload(
  id: UUID,
  name: String,
)

object NamespacePayload {
  given Encoder[NamespacePayload] = deriveEncoder[NamespacePayload]
}
