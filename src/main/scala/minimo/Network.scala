package minimo

import net.liftweb.json.JsonAST.JObject
import rxsocket.presentation.json.{IdentityTask, JProtocol}
import rxsocket.session.ServerEntrance

/**
  *
  */
object Network {
  //socket init
  val conntected = new ServerEntrance(Config.hostIp, Config.hostPort).listen
  val readX = conntected.map(c => (c, c.startReading))

  val readerJProt = readX.map(cx => new JProtocol(cx._1, cx._2))

  case class OverviewRsp(result: Option[OverviewContent], taskId: String) extends IdentityTask
  case class OverviewContent(id: String)
 
  readerJProt.subscribe ( s =>
    s.jRead.subscribe{ j =>
      val jo = j.asInstanceOf[JObject]
      val tsk = jo.\("taskId").values.toString
      //      log(s"get jProto - $tsk")
      s.send(OverviewRsp(Some(OverviewContent("id")), tsk))
      s.send(OverviewRsp(None, tsk))
    }
  )
}
