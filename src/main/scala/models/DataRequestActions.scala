package io.blindnet.dataaccess
package models

import io.circe.{Decoder, Encoder}

object DataRequestActions extends Enumeration {
  type DataRequestAction = Value

  val Get, Delete = Value

  private val byLowerName = DataRequestActions.values.map(e => (e.toString.toLowerCase, e)).toMap
  implicit val decoder: Decoder[DataRequestActions.DataRequestAction] =
    Decoder.decodeString.emap[DataRequestActions.DataRequestAction](k =>
      byLowerName.get(k.toLowerCase).toRight("illegal DataRequestAction value"))
  implicit val encoder: Encoder[DataRequestActions.DataRequestAction] =
    Encoder.encodeString.contramap(_.toString.toLowerCase)
}
