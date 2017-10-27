package minimo.rxsocket

/**
  * 在此实现一颗Map Reduce的服务器端架构。
  * eg. Json Socket Protocol
  * case class JsonRequest(jObj: JObject) // request entity
  * case class ConnectedSocket() // a  socket connection
  * case class JProtocol(cs: ConnectedSocket) // a connection, communicate data based on Json protocol
  * Request - socket -> Client Side | --> | Server Machine --> Filter[JsonRequest]
  */
package object serviceold {

}
