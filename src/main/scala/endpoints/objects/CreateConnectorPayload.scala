package io.blindnet.dataaccess
package endpoints.objects

import io.circe.*
import io.circe.generic.semiauto.*

case class CreateConnectorPayload(
  name: String,
  typ: Option[String],
  config: Option[String],
)

object CreateConnectorPayload {
  given Decoder[CreateConnectorPayload] = Decoder
    .forProduct3("name", "type", "config")(CreateConnectorPayload.apply)
}
