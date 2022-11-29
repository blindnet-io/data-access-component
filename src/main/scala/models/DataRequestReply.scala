package io.blindnet.dataaccess
package models

import io.circe.{Decoder, Encoder}

enum DataRequestReply {
  case ACCEPT, DENY
}

object DataRequestReply {
  private val byLowerName = DataRequestReply.values.map(e => (e.toString.toLowerCase, e)).toMap
  given Decoder[DataRequestReply] =
    Decoder.decodeString.emap[DataRequestReply](k =>
      byLowerName.get(k.toLowerCase).toRight("illegal DataRequestReply value"))
  given Encoder[DataRequestReply] =
    Encoder.encodeString.contramap(_.toString.toLowerCase)
}
