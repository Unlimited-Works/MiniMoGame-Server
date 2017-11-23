package minimo.route

import org.json4s.DefaultFormats
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.slf4j.LoggerFactory
import rxsocket.presentation.json.{EndPoint, RawEndPoint, Router}

import scala.util.Success

/**
  *
  */
class LoginRouter extends Router {
  private val logger = LoggerFactory.getLogger(getClass)
  implicit val formats = DefaultFormats

  override val path = "login"

  override def apply(reqJson: JValue): EndPoint = {
    logger.debug("getjson - " + reqJson)
    val JString(protoId) = reqJson \ "protoId"
    protoId match {
      case LOGIN_PROTO =>
        val LoginProtoReq(username, password) = (reqJson \ "load").extract[LoginProtoReq] //.as[JObject].extract[LoginProtoReq]

        //do database search
        val jsonRsp: JValue =
          if(username == "admin" && password == "admin") {
            true
          } else {
            false
          }

        RawEndPoint(Success(jsonRsp))
    }
  }
}

case class LoginProtoReq(userName: String, password: String)