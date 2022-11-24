package io.blindnet.dataaccess
package services

import endpoints.objects.*
import errors.*
import models.*

import cats.effect.*
import cats.effect.std.UUIDGen

import java.util.UUID
import scala.util.Random

class ConfigurationService(repos: Repositories) {
  val uuidGen: UUIDGen[IO] = UUIDGen.fromSync

  def generateStaticToken(): IO[String] =
    IO(Random.alphanumeric.take(128).mkString)

  def getToken(app: App)(x: Unit): IO[String] =
    IO.pure(app.token)

  def resetToken(app: App)(x: Unit): IO[String] =
    for {
      token <- generateStaticToken()
      _ <- repos.apps.updateToken(app.id, token)
    } yield token

  def createConnector(app: App)(payload: CreateConnectorPayload): IO[ConnectorPayload] =
    for {
      id <- uuidGen.randomUUID
      token <- generateStaticToken()
      co = payload.typ match
        case Some(typ) => GlobalConnector(id, app.id, payload.name, typ, payload.config)
        case None => CustomConnector(id, app.id, payload.name, token)
      _ <- repos.connectors.insert(co)
    } yield ConnectorPayload(co)

  def getConnectors(app: App)(x: Unit): IO[List[ConnectorPayload]] =
    for {
      connectors <- repos.connectors.findAllByApp(app.id)
    } yield connectors.map(ConnectorPayload(_))

  def getConnector(app: App)(id: UUID): IO[ConnectorPayload] =
    for {
      co <- repos.connectors.findById(app.id, id).orNotFound
    } yield ConnectorPayload(co)

  def getConnectorToken(app: App)(id: UUID): IO[String] =
    for {
      co <- repos.connectors.findById(app.id, id).orNotFound
      token <- co match
        case _: GlobalConnector => IO.raiseError(BadRequestException("Not a custom connector"))
        case CustomConnector(_, _, _, token) => IO.pure(token)
    } yield token

  def resetConnectorToken(app: App)(id: UUID): IO[String] =
    for {
      co <- repos.connectors.findById(app.id, id).orNotFound
      _ <- co match
        case _: GlobalConnector => IO.raiseError(BadRequestException("Not a custom connector"))
        case _: CustomConnector => IO.unit
      token <- generateStaticToken()
      _ <- repos.connectors.updateToken(app.id, id, token)
    } yield token
}
