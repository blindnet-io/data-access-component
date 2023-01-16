package io.blindnet.dataaccess
package endpoints.auth

import models.{App, Connector}
import services.ConfigurationService

import cats.effect.IO
import cats.effect.std.UUIDGen
import io.blindnet.identityclient.auth.*

type JwtIdentityAuthenticator = JwtLocalAuthenticator[App]

object JwtIdentityAuthenticator {

  def apply(
    repos: Repositories,
    configurationService: ConfigurationService,
    authenticator: JwtLocalAuthenticator[AppJwt]
  ): JwtIdentityAuthenticator =
    authenticator.mapJwtF(jwt =>
      repos.apps.findById(jwt.appId).flatMap(_ match
        case Some(app) => IO.pure(app)
        case None => for {
          appToken <- configurationService.generateStaticToken()
          app = App(jwt.appId, appToken)
          _ <- repos.apps.insert(app)
        } yield app))
}
