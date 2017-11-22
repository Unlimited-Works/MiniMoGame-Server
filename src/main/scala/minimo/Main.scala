package minimo

import minimo.viewfirst.login.RouterLogin
import org.slf4j.LoggerFactory

/**
  *
  */
object Main extends App {
  private val logger = LoggerFactory.getLogger(this.getClass)

  //init config and print config information
  MinimoConfig

  //forbid heart beat for simple
  rxsocket.session.Configration.CHECK_HEART_BEAT_BREAKTIME = Int.MaxValue
  rxsocket.session.Configration.SEND_HEART_BEAT_BREAKTIME = Int.MaxValue

  //start network server
  logger.info(s"starting network at: ${MinimoConfig.network}")
  new Network(MinimoConfig.network.host, MinimoConfig.network.port, new RouterLogin :: Nil)

  Thread.currentThread().join()
}
