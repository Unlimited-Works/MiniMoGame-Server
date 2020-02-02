package minimo.network.jsonsocket

import minimo.rxsocket.session.OnSocketCloseMsg

import scala.util.Try

trait JProtoEvent
object JProtoEvent {
  case class SocketDisconnect(value: Try[OnSocketCloseMsg]) extends JProtoEvent
}
