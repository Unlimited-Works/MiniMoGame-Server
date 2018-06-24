package minimo.rxsocket.presentation.json

import org.json4s.JsonAST.{JString, JValue}

import scala.collection.mutable
import scala.concurrent.Promise


/**
  * router define how to deal with received data
  */
trait JRouter {//extends (JValue => EndPoint)
  val jsonPath: String
  def jsonRoute(protoId: String, load: JValue): EndPoint

}

class JRouterManager {

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
  def dispatch(load: JValue): EndPoint = {
    val JString(path) = load \ "path"
    val JString(protoId) = load \ "protoId"

    val route = routes(path)
    route.jsonRoute(protoId, load \ "load")
  }
}

