package minimo


import org.slf4j.{Logger, LoggerFactory}
import lorance.rxsocket.presentation.json._
import lorance.rxsocket.session.{ConnectedSocket, ServerEntrance}
import monix.reactive.Observable

/**
  *
  */
class Network(host: String, port: Int, routes: List[Router]) {
  protected val logger = LoggerFactory.getLogger(getClass)
  //socket init
  val conntected: Observable[ConnectedSocket] = new ServerEntrance(host, port).listen

  val readerJProt: Observable[JProtocol] = conntected.map(cx => new JProtocol(cx, cx.startReading))

  //register service
  val jProtoServer: JProtoServer = new JProtoServer(readerJProt, routes)

}
