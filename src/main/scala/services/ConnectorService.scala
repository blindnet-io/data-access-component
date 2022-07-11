package io.blindnet.dataaccess
package services

import cats.effect.IO
import fs2.Pipe

class ConnectorService {
  def ws(x: Unit): IO[Pipe[IO, String, String]] = ???
}
