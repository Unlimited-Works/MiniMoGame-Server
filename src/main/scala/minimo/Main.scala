package minimo

import minimo.network.Network
import org.slf4j.LoggerFactory
import route.{LoginRouter, RoomRouter}

/**
  *
  */
object Main extends App {
  private val logger = LoggerFactory.getLogger(this.getClass)

  //init config
  MinimoConfig

  //forbid heart beat for simple
//  lorance.rxsocket.session.Configration.CHECK_HEART_BEAT_BREAKTIME = Int.MaxValue
//  lorance.rxsocket.session.Configration.SEND_HEART_BEAT_BREAKTIME = Int.MaxValue
  val routes = List(
    new LoginRouter,
    new RoomRouter,

  )

  //start network server
  logger.info(s"starting network at: ${MinimoConfig.network}")
  new Network(
    MinimoConfig.network.host,
    MinimoConfig.network.port,
    routes)

  Thread.currentThread().join()
}
