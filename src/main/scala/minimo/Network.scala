package minimo

import net.liftweb.json.JsonAST.JObject
import rx.lang.scala.Observable
import rxsocket.presentation.json.{IdentityTask, JProtocol}
import rxsocket.session.ServerEntrance

import scala.concurrent.Future

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
    s.jRead.subscribe{ jValue =>
      val jObj = jValue.asInstanceOf[JObject]
      Router.dispatch(jObj).foreach(rstJObj => s.send(rstJObj))
    }
  )
}

trait Router extends (JObject => Observable[JObject]) {
  val path: String
  def register: Unit = {
    Router.routes += (path -> this)
  }
}

object Router {
  val routes = collection.mutable.HashMap[String, Router]()

  /**
    * one request map to a observable stream
    * @param jObject
    * @return
    */
  def dispatch(jObject: JObject): Observable[JObject] = {
    val path = (jObject \ "path" values).toString
    val route = routes(path)
    val result = route(jObject)

    result
  }
}