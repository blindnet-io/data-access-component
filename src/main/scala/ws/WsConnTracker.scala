package io.blindnet.dataaccess
package ws

case class WsConnTracker(connections: Vector[WsConnection] = Vector.empty) {
  private val roundRobin = Iterator.continually(connections).flatten
  def get: Option[WsConnection] = roundRobin.nextOption()
  
  def add(connection: WsConnection): WsConnTracker =
    copy(connections = connections.appended(connection))

  def remove(connection: WsConnection): WsConnTracker =
    copy(connections = connections.filterNot(_ == connection))
}
