package minimo.viewfirst.login

import minimo.Router
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{JField, JObject, JString}
import rx.lang.scala.Subject

/**
  *
  */
class RouterLogin extends Router {

  override val path = "login"

  override def apply(reqJson: JsonAST.JObject) = {
    (reqJson \ "protoId" values).toString match {
      case PROTO_LOGIN =>
        reqJson \ "load" values
        val obv = Subject[JObject]
        obv.onNext(JObject(JField("testName", JString("testValue"))))
        obv.onCompleted()
        obv
    }
  }
}
