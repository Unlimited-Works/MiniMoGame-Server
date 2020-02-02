package minimo.rxsocket.actor

import scala.concurrent.Future

/**
  * a simple Actor class handle message by ConcurrentHashMap
  */
trait Actor {

  def send(to: Actor, msg: Object): Unit = {
    val actor = mailBox.get(to)
    if(actor != null) {
      actor
    }
  }
  def sendWithRsp(to: Actor, msg: Object): Future[Object]

}
