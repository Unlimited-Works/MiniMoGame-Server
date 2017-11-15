package minimo

import minimo.network._
import minimo.viewfirst.login.RouterLogin
import org.json4s.JsonAST.{JObject, JString, JValue}
import org.json4s.JsonDSL._
import org.slf4j.LoggerFactory
import rx.lang.scala.Observable
import rxsocket.presentation.json.{IdentityTask, JProtocol}
import rxsocket.session.ServerEntrance

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  *
  */
class Network(routes: List[Router]) {
  val logger = LoggerFactory.getLogger(getClass)
  //socket init
  val conntected = new ServerEntrance(Config.hostIp, Config.hostPort).listen
  val readX = conntected.map(c => (c, c.startReading))

  val readerJProt = readX.map(cx => new JProtocol(cx._1, cx._2))

  //register service
//  new RouterLogin
  routes.foreach(_.register)
  //handle streams
  readerJProt.subscribe ( skt =>
    skt.jRead.subscribe{ jValue =>

      /**
        * protocol form client:
        * {
        *   taskId: ...
        *   load: {
        *     ...
        *   }
        * }
        */
      val load = jValue \ "load"
      val taskId = jValue \ "taskId"
      val endPoint = Router.dispatch(load)

      logger.debug("result message - " + endPoint)
      /**
        * protocol to client:
        * {
        *   taskId: ...
        *   type: ...
        *   status: ...
        *   load: {
        *     ...
        *   }
        * }
        */
      endPoint match {
        case RawEndPoint(jValRst) =>
          jValRst match {
            case Failure(e) =>
              val finalJson =
                ("taskId" -> taskId) ~
                ("type" -> "once") ~
                ("status" -> "error") ~
                ("load" -> e.getStackTrace.toString)
              skt.send(finalJson)
            case Success(rst) =>
              val finalJson =
                ("taskId" -> taskId) ~
                  ("type" -> "once") ~
                  ("status" -> "end") ~
                  ("load" -> rst)
              skt.send(finalJson)
          }
        case FurEndPoint(jValRstFur) =>
          jValRstFur.foreach(jValRst => {
            val finalJson =
              ("taskId" -> taskId) ~
              ("type" -> "once") ~
              ("status" -> "end") ~
              ("load" -> jValRst)
            skt.send(finalJson)
          })
          jValRstFur.failed.foreach{ error =>
            logger.error("FurEndPoint failed:", error)

            val finalJson =
              ("taskId" -> taskId) ~
              ("type" -> "once") ~
              ("status" -> "error") ~
              ("load" -> error.getStackTrace.mkString)
            skt.send(finalJson)
          }
        case StreamEndPoint(jValRst) =>
          jValRst.subscribe(onNext = event => {
            val finalJson: JValue =
              ("taskId" -> taskId) ~
              ("type" -> "stream") ~
              ("status" -> "on") ~
              ("load" -> event)

            skt.send(finalJson)
          },
          onError = error => {
            logger.error("StreamEndPoint failed:", error)
            val finalJson: JValue =
              ("taskId" -> taskId) ~
              ("type" -> "stream") ~
              ("status" -> "error") ~
              ("load" -> error.getStackTrace.mkString)

            skt.send(finalJson)
          },
          onCompleted = () => {
            val finalJson: JValue =
              ("taskId" -> taskId) ~
              ("type" -> "stream") ~
              ("status" -> "end")

            skt.send(finalJson)
          })
      }
    }
  )
}

/**
  * router define how to deal with received data
  */
trait Router extends (JValue => EndPoint) {
  val path: String

  lazy val register: Unit = {
    Router.routes += (path -> this)
  }

}

object Router {

  val routes = collection.mutable.HashMap[String, Router]()

  /**
    * one request map to a observable stream
    * load protocol, eg:
    * {
    *   path: "Login",
    *   protoId: "LOGIN_PROTO",
    *   load: {
    *     ...
    *   }
    * }
    * @param load: message client send here
    * @return
    */
  def dispatch(load: JValue): EndPoint = {
    val JString(path) = load \ "path"

    val route = routes(path)
    route(load)
  }
}

