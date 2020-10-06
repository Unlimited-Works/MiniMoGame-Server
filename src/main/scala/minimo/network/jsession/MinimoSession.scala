package minimo.network.jsession

import java.util.concurrent.ConcurrentHashMap

import minimo.rxsocket.presentation.json.JProtocol

import scala.collection.mutable.{Map => MutMap}

// todo put and get data use trait + case class, similar as
case class MinimoSession(sessionId: String, data: MutMap[String, Any]) {

  def updateData[T](f: MutMap[String, Any] => T): T = {
    sessionId.synchronized {
      f(data)
    }
  }

  def getData[T](f: MutMap[String, Any] => T): T = {
    sessionId.synchronized {
      f(data)
    }
  }

}

object MinimoSession {
  private val minimoSessions = new ConcurrentHashMap[String, MinimoSession]()
  def apply(sessionId: String, skt: JProtocol, data:  MutMap[String, Any]) = {
    val minimoSession = new MinimoSession(sessionId, data)

    minimoSessions.compute(sessionId, (_,v) => {
      assert(v == null, "已存在")
      minimoSession
    })

    minimoSession
  }

  def clear(sessionId: String) = {
    minimoSessions.remove(sessionId)
  }

  def findById(sessionId: String): Option[MinimoSession] = {
    val v = minimoSessions.get(sessionId)
    Option(v)
  }


}