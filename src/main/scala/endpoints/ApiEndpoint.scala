package io.blindnet.dataaccess
package endpoints

import cats.effect.IO
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint

type ApiEndpoint = ServerEndpoint[Fs2Streams[IO] with WebSockets, IO]
