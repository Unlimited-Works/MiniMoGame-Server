package minimo.viewfirst.login

import minimo.Router
import minimo.network._
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.{DefaultFormats, JsonAST}

import scala.util.{Success, Try}

/**
  *
  */
class RouterLogin extends Router {
  implicit val formats = DefaultFormats

  override val path = "login"

  override def apply(reqJson: JValue): EndPoint = {
    val JString(protoId) = reqJson \ "protoId"
    protoId match {
      case PROTO_LOGIN =>
        val LoginProtoReq(username, password) = (reqJson \ "load" values).asInstanceOf[JObject].extract[LoginProtoReq]

        //do database search
        val jsonRsp: JValue =
          if(username == "admin" && password == "admin") {
            "login" -> true
          } else {
            "result" -> false
          }

        RawEndPoint(Success(jsonRsp))
    }
  }
}

case class LoginProtoReq(username: String, password: String)