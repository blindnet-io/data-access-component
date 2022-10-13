package io.blindnet.dataaccess
package endpoints.auth

import models.App

import io.blindnet.identityclient.auth.JwtAuthenticator

type JwtAppAuthenticator = JwtAuthenticator[App]
