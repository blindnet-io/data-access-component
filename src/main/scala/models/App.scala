package io.blindnet.dataaccess
package models

import io.blindnet.identityclient.auth.St

import java.util.UUID

case class App(
  id: UUID,
  token: String
) extends St
