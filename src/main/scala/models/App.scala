package io.blindnet.dataaccess
package models

import io.blindnet.identityclient.auth.St

case class App(
  id: String,
  token: String
) extends St
