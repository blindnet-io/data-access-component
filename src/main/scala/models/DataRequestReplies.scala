package io.blindnet.dataaccess
package models

import io.circe.{Decoder, Encoder}

object DataRequestReplies extends Enumeration {
  type DataRequestReply = Value

  val Accept, Deny = Value

  private val byLowerName = DataRequestReplies.values.map(e => (e.toString.toLowerCase, e)).toMap
  implicit val decoder: Decoder[DataRequestReplies.DataRequestReply] =
    Decoder.decodeString.emap[DataRequestReplies.DataRequestReply](k =>
      byLowerName.get(k.toLowerCase).toRight("illegal DataRequestReply value"))
  implicit val encoder: Encoder[DataRequestReplies.DataRequestReply] =
    Encoder.encodeString.contramap(_.toString.toLowerCase)
}
