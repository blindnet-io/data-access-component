package io.blindnet.dataaccess
package endpoints.auth

import models.Connector

import io.blindnet.identityclient.auth.StAuthenticator

type ConnectorAuthenticator = StAuthenticator[Connector, Connector]
