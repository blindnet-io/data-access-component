package io.blindnet.dataaccess
package ws.packets.in

import io.circe.*

object DataReplies extends Enumeration {
  type DataReply = Value

  val Accept, Deny = Value

  private val byLowerName = DataReplies.values.map(e => (e.toString.toLowerCase, e)).toMap
  implicit val decoder: Decoder[DataReplies.DataReply] =
    Decoder.decodeString.emap[DataReplies.DataReply](k => byLowerName.get(k.toLowerCase).toRight("illegal DataReply value"))
  implicit val encoder: Encoder[DataReplies.DataReply] =
    Encoder.encodeString.contramap(_.toString.toLowerCase)
}
