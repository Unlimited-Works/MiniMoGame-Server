package minimo.network

import lorance.rxsocket.presentation.json._
import lorance.rxsocket.session.{CommPassiveParser, CompletedProto, ConnectedSocket, ServerEntrance}
import monix.reactive.Observable
import org.slf4j.LoggerFactory

/**
  *
  */
class Network(host: String, port: Int, routes: List[Router]) {
  protected val logger = LoggerFactory.getLogger(getClass)
  //socket init
  val conntected: Observable[ConnectedSocket[CompletedProto]] = new ServerEntrance(host, port, new CommPassiveParser()).listen

  val readerJProt: Observable[JProtocol] = conntected.map(cx => new JProtocol(cx, cx.startReading))

  //register service
  val jProtoServer: JProtoServer = new JProtoServer(readerJProt, routes)

}
