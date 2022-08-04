package io.blindnet.dataaccess
package services

import ws.WsConnection

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*

class ConnectorService(connections: Ref[IO, List[WsConnection]]) {
  def ws(x: Unit): IO[Pipe[IO, String, String]] =
    for {
      queue <- Queue.unbounded[IO, String]
      conn = WsConnection(queue)
      _ <- connections.update(l => conn :: Nil) // TODO don't keep only one conn
    } yield (in: Stream[IO, String]) => {
      Stream.fromQueueUnterminated(queue, Int.MaxValue)
        .mergeHaltBoth(in.evalTap(conn.receive).drain)
    }

  // TODO by-connector tracking
  def connection: IO[WsConnection] =
    connections.get.map(_.head)
}

object ConnectorService {
  def apply(): IO[ConnectorService] =
    Ref[IO].of[List[WsConnection]](Nil).map(ref => new ConnectorService(ref))
}
