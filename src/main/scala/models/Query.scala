package io.blindnet.dataaccess
package models

import ws.packets.in.*

import io.circe.*
import io.circe.generic.semiauto.*

case class Query(
  id: String,
  callback: String,
  reply: Option[DataReplies.DataReply] = None,
  dataPath: Option[String] = None,
) {
  implicit val encoder: Encoder[Query] = deriveEncoder[Query]
  implicit val decoder: Decoder[Query] = deriveDecoder[Query]
}
