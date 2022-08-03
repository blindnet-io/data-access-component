package io.blindnet.dataaccess
package ws.packets

import io.circe.*

object DataActions extends Enumeration {
  type DataAction = Value

  val Get, Delete = Value
}
implicit val eDataActions: Encoder[DataActions.DataAction] =
  Encoder.encodeString.contramap[DataActions.Value](_.toString.toLowerCase)
