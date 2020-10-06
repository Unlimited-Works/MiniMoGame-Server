package minimo.network.jsonsocket

import minimo.exception.BizException
import minimo.network.jsession.MinimoSession
import org.json4s.JsonAST.{JString, JValue}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

/**
  * router define how to deal with received data
  */
trait JRouter {
  val jsonPath: String
  def jsonRoute(protoId: String, load: JValue)(implicit session: MinimoSession): EndPoint

  /**
    * deal with non-network protocol message to the router.
    * event might be a exception
    */
  def onEvent(event: JProtoEvent)(implicit session: MinimoSession): Future[Unit] = {Future.successful(())}

}

class JRouterManager {
  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  val routes: mutable.HashMap[String, JRouter] = collection.mutable.HashMap[String, JRouter]()

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
  def dispatch(path: String, protoId: String, load: JValue)(implicit session: MinimoSession): EndPoint = {
    try {
//      val JString(path) = load \ "path"
//      val JString(protoId) = load \ "protoId"

      val route = routes(path)
      route.jsonRoute(protoId, load)
    } catch {
      case bex @ BizException(code, desc) =>
        logger.error("jproto dispatch error: ", bex)
        ErrorEndPoint(ErrorValue(code.toString, desc, bex))
      case NonFatal(ex) =>
        logger.error("dispatch exception: ", ex)
        SystemErrorEndPoint(ex)
    }
  }
}

