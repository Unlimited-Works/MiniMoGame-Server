package minimo.network.jsession

import minimo.network.jsession.JSession.JSessionKey

/**
  * a JSession is represent by a connected jprotocol socket stateful data storage.
  * It should be destroyed after socket disconnected.
  * JSession mainly pair with JRouter
  */
trait JSession {
//  val sessionId: String
//  implicit val session: MinimoSession = MinimoSession(sessionId, mutable.Map[String, Any]())
  val module: String
  def clearSession(): Unit

  def put[T](jSessionKey: JSessionKey, value: Any)(implicit minimoSession: MinimoSession) = {

  }

}

object JSession {
  // use case object extends jsessionkey
  trait JSessionKey
}