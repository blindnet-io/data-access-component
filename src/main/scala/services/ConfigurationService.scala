package io.blindnet.dataaccess
package services

import endpoints.objects.*
import errors.*
import models.*

import cats.effect.*

import java.util.UUID
import scala.util.Random

class ConfigurationService(repos: Repositories) {
  def generateStaticToken(): IO[String] =
    IO(Random.alphanumeric.take(128).mkString)

  def getToken(app: App)(x: Unit): IO[String] =
    IO.pure(app.token)

  def resetToken(app: App)(x: Unit): IO[String] =
    for {
      token <- generateStaticToken()
      _ <- repos.apps.updateToken(app.id, token)
    } yield token

  def getNamespaces(app: App)(x: Unit): IO[List[NamespacePayload]] =
    for {
      namespaces <- repos.namespaces.findAllByApp(app.id)
    } yield namespaces.map(ns => NamespacePayload(ns.id, ns.name))

  def getNamespace(app: App)(id: UUID): IO[NamespacePayload] =
    for {
      ns <- repos.namespaces.findById(app.id, id).orNotFound
    } yield NamespacePayload(ns.id, ns.name)

  def getNamespaceToken(app: App)(id: UUID): IO[String] =
    for {
      ns <- repos.namespaces.findById(app.id, id).orNotFound
    } yield ns.token

  def resetNamespaceToken(app: App)(id: UUID): IO[String] =
    for {
      ns <- repos.namespaces.findById(app.id, id).orNotFound
      token <- generateStaticToken()
      _ <- repos.namespaces.updateToken(app.id, id, token)
    } yield token
}
