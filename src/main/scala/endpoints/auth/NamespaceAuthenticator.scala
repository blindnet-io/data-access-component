package io.blindnet.dataaccess
package endpoints.auth

import models.Namespace

import io.blindnet.identityclient.auth.StAuthenticator

type NamespaceAuthenticator = StAuthenticator[Namespace, Namespace]
