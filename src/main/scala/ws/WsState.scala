package io.blindnet.dataaccess
package ws

import models.{Connector, CustomConnector, GlobalConnector}
import ws.*

import cats.data.OptionT
import cats.effect.*
import io.circe.Encoder

import java.util.UUID

class WsState(
  private val custom: Ref[IO, Map[UUID, WsConnTracker]],
  private val global: Ref[IO, WsConnTracker],
) {
  private def customTracker(co: CustomConnector): IO[WsConnTracker] =
    for {
      existing <- custom.get.map(_.get(co.id))
      tracker <- existing match
        case Some(value) => IO.pure(value)
        case None =>
          val newTracker = WsConnTracker()
          custom.update(_ + (co.id -> newTracker)).as(newTracker)
    } yield tracker
    
  private def updateCustomTracker(co: CustomConnector, f: WsConnTracker => WsConnTracker): IO[Unit] =
    custom.update(map => map + (co.id -> f(map.getOrElse(co.id, WsConnTracker()))))

  def addCustomConnection(co: CustomConnector, conn: WsConnection): IO[Unit] =
    updateCustomTracker(co, _.add(conn))

  def addGlobalConnection(conn: WsConnection): IO[Unit] =
    global.update(_.add(conn))

  def removeCustomConnection(co: CustomConnector, conn: WsConnection): IO[Unit] =
    updateCustomTracker(co, _.remove(conn))

  def removeGlobalConnection(conn: WsConnection): IO[Unit] =
    global.update(_.remove(conn))

  def customConnection(co: CustomConnector): IO[WsConnection] =
    customTracker(co).map(_.get.get)

  def globalConnection(): IO[WsConnection] =
    global.get.map(_.get.get)

  def send[T <: WsOutPacket](co: Connector, packet: T)(implicit enc: Encoder[T]): IO[Unit] =
    co match
      case global: GlobalConnector => globalConnection().flatMap(_.sendGlobal(global, packet))
      case custom: CustomConnector => customConnection(custom).flatMap(_.send(packet))
}

object WsState {
  def apply(): IO[WsState] =
    for {
      custom <- Ref[IO].of[Map[UUID, WsConnTracker]](Map.empty)
      global <- Ref[IO].of[WsConnTracker](WsConnTracker())
    } yield new WsState(custom, global)
}
