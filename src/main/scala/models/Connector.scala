package io.blindnet.dataaccess
package models

import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.Read
import io.blindnet.identityclient.auth.St
import io.circe.*
import io.circe.generic.semiauto.*

import java.util.UUID

sealed trait Connector {
  def id: UUID
  def appId: UUID
  def name: String
}

object Connector {
  given Read[Connector] =
    Read[(UUID, UUID, String, Option[String], Option[String], Option[String])]
      .map((id, appId, name, typOption, configOption, tokenOption) => {
        typOption match
          case Some(typ) => GlobalConnector(id, appId, name, typ, configOption)
          case None => CustomConnector(id, appId, name, tokenOption.get)
      })

  given Write[Connector] =
    Write[(UUID, UUID, String, Option[String], Option[String], Option[String])]
      .contramap(_ match
        case GlobalConnector(id, appId, name, typ, config) => (id, appId, name, Some(typ), config, None)
        case CustomConnector(id, appId, name, token) => (id, appId, name, None, None, Some(token)))
}

case class GlobalConnector(
  id: UUID,
  appId: UUID,
  name: String,
  typ: String,
  config: Option[String],
) extends Connector

case class CustomConnector(
  id: UUID,
  appId: UUID,
  name: String,
  token: String,
) extends Connector, St
