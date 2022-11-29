package io.blindnet.dataaccess
package ws

case class WsConnTracker[T <: WsConnection](connections: Vector[T] = Vector.empty[T]) {
  private val roundRobin = Iterator.continually(connections).flatten
  def get: Option[T] = roundRobin.nextOption()
  
  def add(connection: T): WsConnTracker[T] =
    copy(connections = connections.appended(connection))

  def remove(connection: T): WsConnTracker[T] =
    copy(connections = connections.filterNot(_ == connection))
}
