package io.blindnet.dataaccess
package services

import redis.DataRequestRepository
import ws.WsConnection

import cats.effect.*
import cats.effect.std.*
import fs2.*
import fs2.concurrent.*

import java.nio.ByteBuffer

class ConnectorService(queryRepo: DataRequestRepository, connections: Ref[IO, List[WsConnection]]) {
  def ws(x: Unit): IO[Pipe[IO, Array[Byte], Array[Byte]]] =
    for {
      queue <- Queue.unbounded[IO, Array[Byte]]
      conn = WsConnection(queryRepo, queue)
      _ <- connections.update(l => conn :: Nil) // TODO don't keep only one conn
    } yield (in: Stream[IO, Array[Byte]]) => {
      Stream.fromQueueUnterminated(queue, Int.MaxValue)
        .mergeHaltBoth(in.map(ByteBuffer.wrap).evalTap(conn.receive).drain)
    }

  // TODO by-connector tracking
  def connection: IO[WsConnection] =
    connections.get.map(_.head)
}

object ConnectorService {
  def apply(queryRepo: DataRequestRepository): IO[ConnectorService] =
    Ref[IO].of[List[WsConnection]](Nil).map(ref => new ConnectorService(queryRepo, ref))
}
