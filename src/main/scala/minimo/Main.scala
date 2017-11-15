package minimo

import minimo.viewfirst.login.RouterLogin

/**
  *
  */
object Main extends App {
  //init various resource
  Config

  //forbid heart beat for simple
  rxsocket.session.Configration.CHECK_HEART_BEAT_BREAKTIME = Int.MaxValue
  rxsocket.session.Configration.SEND_HEART_BEAT_BREAKTIME = Int.MaxValue

  //start network server
  new Network(new RouterLogin :: Nil)

  Thread.currentThread().join()
}
