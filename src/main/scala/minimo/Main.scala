package minimo

import org.slf4j.LoggerFactory
import route.LoginRouter

/**
  *
  */
object Main extends App {
  private val logger = LoggerFactory.getLogger(this.getClass)

  //init config
  MinimoConfig

  //forbid heart beat for simple
  rxsocket.session.Configration.CHECK_HEART_BEAT_BREAKTIME = Int.MaxValue
  rxsocket.session.Configration.SEND_HEART_BEAT_BREAKTIME = Int.MaxValue

  //start network server
  logger.info(s"starting network at: ${MinimoConfig.network}")
  new Network(MinimoConfig.network.host, MinimoConfig.network.port, new LoginRouter :: Nil)

  Thread.currentThread().join()
}
