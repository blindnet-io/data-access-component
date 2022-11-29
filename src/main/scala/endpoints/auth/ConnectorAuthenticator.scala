package io.blindnet.dataaccess
package endpoints.auth

import models.CustomConnector

import io.blindnet.identityclient.auth.StAuthenticator

type ConnectorAuthenticator = StAuthenticator[CustomConnector, CustomConnector]
