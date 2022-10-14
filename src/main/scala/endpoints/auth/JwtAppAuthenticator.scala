package io.blindnet.dataaccess
package endpoints.auth

import models.{App, Namespace}
import services.ConfigurationService

import cats.effect.IO
import cats.effect.std.UUIDGen
import io.blindnet.identityclient.auth.*

type JwtAppAuthenticator = JwtAuthenticator[App]

object JwtAppAuthenticator {
  val uuidGen: UUIDGen[IO] = UUIDGen.fromSync

  def apply(repos: Repositories, configurationService: ConfigurationService, authenticator: JwtAuthenticator[Jwt]): JwtAppAuthenticator =
    authenticator.requireAppJwt.mapJwtF(jwt =>
      repos.apps.findById(jwt.appId).flatMap(_ match
        case Some(app) => IO.pure(app)
        case None => for {
          appToken <- configurationService.generateStaticToken()
          app = App(jwt.appId, appToken)
          _ <- repos.apps.insert(app)

          nsId <- uuidGen.randomUUID
          nsToken <- configurationService.generateStaticToken()
          ns = Namespace(nsId, app.id, "Default", nsToken)
          _ <- repos.namespaces.insert(ns)
        } yield app))
}
