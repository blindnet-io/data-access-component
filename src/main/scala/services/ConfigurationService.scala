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
      co = Connector(id, app.id, payload.name, payload.typ, payload.config, token)
      _ <- repos.connectors.insert(co)
    } yield ConnectorPayload(co.id, co.name, co.typ, co.config)

  def getConnectors(app: App)(x: Unit): IO[List[ConnectorPayload]] =
    for {
      connectors <- repos.connectors.findAllByApp(app.id)
    } yield connectors.map(co => ConnectorPayload(co.id, co.name, co.typ, co.config))

  def getConnector(app: App)(id: UUID): IO[ConnectorPayload] =
    for {
      co <- repos.connectors.findById(app.id, id).orNotFound
    } yield ConnectorPayload(co.id, co.name, co.typ, co.config)

  def getConnectorToken(app: App)(id: UUID): IO[String] =
    for {
      co <- repos.connectors.findById(app.id, id).orNotFound
      _ <- co.typ.thenBadRequest("Not a custom connector")
    } yield co.token

  def resetConnectorToken(app: App)(id: UUID): IO[String] =
    for {
      co <- repos.connectors.findById(app.id, id).orNotFound
      _ <- co.typ.thenBadRequest("Not a custom connector")
      token <- generateStaticToken()
      _ <- repos.connectors.updateToken(app.id, id, token)
    } yield token
}
