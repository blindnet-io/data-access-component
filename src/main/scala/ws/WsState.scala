package io.blindnet.dataaccess
package ws

import models.{Connector, CustomConnector, GlobalConnector}
import ws.*

import cats.data.OptionT
import cats.effect.*
import io.circe.Encoder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

type CustomTracker = WsConnTracker[CustomWsConnection]
type GlobalTracker = WsConnTracker[GlobalWsConnection]

class WsState(
  private val custom: Ref[IO, Map[UUID, CustomTracker]],
  private val global: Ref[IO, Map[String, GlobalTracker]],
  private val logger: Logger[IO]
) {
  private def updateCustomTracker(co: CustomConnector, f: CustomTracker => CustomTracker): IO[Unit] =
    custom.update(map => map + (co.id -> f(map.getOrElse(co.id, WsConnTracker[CustomWsConnection]()))))

  private def updateGlobalTrackers(conn: GlobalWsConnection, f: GlobalTracker => GlobalTracker): IO[Unit] =
    global.update(conn.types.foldLeft(_)((map, typ) => map + (typ -> f(map.getOrElse(typ, WsConnTracker[GlobalWsConnection]())))))

  def addCustomConnection(co: CustomConnector, conn: CustomWsConnection): IO[Unit] =
    updateCustomTracker(co, _.add(conn))
      .flatMap(_ => logger.info(s"Custom connector connected: ${co.appId}/${co.id}"))

  def addGlobalConnection(conn: GlobalWsConnection): IO[Unit] =
    updateGlobalTrackers(conn, _.add(conn))
      .flatMap(_ => logger.info(s"Global connector connected: ${conn.types.mkString(", ")}"))

  def removeCustomConnection(co: CustomConnector, conn: CustomWsConnection): IO[Unit] =
    updateCustomTracker(co, _.remove(conn))
      .flatMap(_ => logger.info(s"Custom connector disconnected: ${co.appId}/${co.id}"))

  def removeGlobalConnection(conn: GlobalWsConnection): IO[Unit] =
    updateGlobalTrackers(conn, _.remove(conn))
      .flatMap(_ => logger.info(s"Global connector disconnected: ${conn.types.mkString(", ")}"))

  def customConnection(co: CustomConnector): IO[CustomWsConnection] =
    custom.get.map(_.get(co.id).flatMap(_.get).get)

  def globalConnection(typ: String): IO[GlobalWsConnection] =
    global.get.map(_.get(typ).flatMap(_.get).get)

  def send[T <: WsOutPacket](co: Connector, packet: T)(using Encoder[T]): IO[Unit] =
    co match
      case global: GlobalConnector => globalConnection(global.typ).flatMap(_.send(global, packet))
      case custom: CustomConnector => customConnection(custom).flatMap(_.send(packet))
}

object WsState {
  def apply(): IO[WsState] =
    for {
      custom <- Ref[IO].of[Map[UUID, CustomTracker]](Map.empty)
      global <- Ref[IO].of[Map[String, GlobalTracker]](Map.empty)
      logger <- Slf4jLogger.create[IO]
    } yield new WsState(custom, global, logger)
}
