package minimo.network

import minimo.rxsocket.presentation.json._
import minimo.rxsocket.session.{CommPassiveParser, CompletedProto, ConnectedSocket, ServerEntrance}
import monix.reactive.Observable
import org.slf4j.LoggerFactory

/**
  * all services start at here
  */
class Network(host: String, port: Int, syncPort: Int, routes: List[JRouter], syncRouters: Map[SyncProto, SyncRouter]) {
  protected val logger = LoggerFactory.getLogger(getClass)
  //socket init
  val conntected: Observable[ConnectedSocket[CompletedProto]] = new ServerEntrance(host, port, () => new CommPassiveParser()).listen

  val readerJProt: Observable[JProtocol] = conntected.map(cx => new JProtocol(cx, cx.startReading))

  //register service
  val jProtoServer: JProtoServer = new JProtoServer(readerJProt, routes)

  val syncServer = new SyncServer(host, syncPort, syncRouters)
}
