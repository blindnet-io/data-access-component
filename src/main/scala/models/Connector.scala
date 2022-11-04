package io.blindnet.dataaccess
package models

import io.blindnet.identityclient.auth.St
import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class Connector(
  id: UUID,
  appId: UUID,
  name: String,
  typ: Option[String],
  config: Option[String],
  token: String,
) extends St
