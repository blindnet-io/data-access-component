package io.blindnet.dataaccess
package models

import io.blindnet.identityclient.auth.St
import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

case class Namespace(
  id: UUID,
  appId: UUID,
  name: String,
  token: String,
) extends St
