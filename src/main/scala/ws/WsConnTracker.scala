package io.blindnet.dataaccess
package ws

case class WsConnTracker(connections: List[WsConnection] = Nil) {
  private val roundRobin = Iterator.continually(connections).flatten
  def get: Option[WsConnection] = roundRobin.nextOption()
  
  def add(connection: WsConnection): WsConnTracker =
    copy(connections = connection :: connections)
}
