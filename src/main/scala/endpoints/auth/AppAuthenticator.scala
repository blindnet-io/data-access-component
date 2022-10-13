package io.blindnet.dataaccess
package endpoints

import models.App

import io.blindnet.identityclient.auth.StAuthenticator

type AppAuthenticator = StAuthenticator[App, App]
