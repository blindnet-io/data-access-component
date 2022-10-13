package io.blindnet.dataaccess
package services

import endpoints.objects.*
import errors.*
import models.*

import cats.effect.*

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
}
