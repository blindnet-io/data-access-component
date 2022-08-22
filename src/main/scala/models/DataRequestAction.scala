package io.blindnet.dataaccess
package models

import io.circe.{Decoder, Encoder}

enum DataRequestAction {
  case GET, DELETE
}

object DataRequestAction {
  private val byLowerName = DataRequestAction.values.map(e => (e.toString.toLowerCase, e)).toMap
  implicit val decoder: Decoder[DataRequestAction] =
    Decoder.decodeString.emap[DataRequestAction](k =>
      byLowerName.get(k.toLowerCase).toRight("illegal DataRequestAction value"))
  implicit val encoder: Encoder[DataRequestAction] =
    Encoder.encodeString.contramap(_.toString.toLowerCase)
}
