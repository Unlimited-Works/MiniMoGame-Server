package minimo


import org.slf4j.LoggerFactory
import lorance.rxsocket.presentation.json._
import lorance.rxsocket.session.ServerEntrance
import monix.reactive.Observable

/**
  *
  */
class Network(host: String, port: Int, routes: List[Router]) {
  val logger = LoggerFactory.getLogger(getClass)
  //socket init
  val conntected = new ServerEntrance(host, port).listen
  val readX = conntected.map(c => (c, c.startReading))

  val readerJProt: Observable[JProtocol] = readX.map(cx => new JProtocol(cx._1, cx._2))

  //register service
  val jProtoServer = new JProtoServer(readerJProt, routes)

}
