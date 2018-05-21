package minimo.route

import org.json4s.DefaultFormats
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.slf4j.LoggerFactory
import lorance.rxsocket.presentation.json.{EmptyEndPoint, EndPoint, JRouter, RawEndPoint}

import scala.util.Success
import minimo.service
import minimo.service.api.UserService

/**
  *
  */
class LoginRouter extends JRouter {
  private val logger = LoggerFactory.getLogger(getClass)
  implicit val formats: DefaultFormats.type = DefaultFormats

  val userSerivce: UserService = service.userService

//  override val path = "login"

  override def apply(reqJson: JValue): EndPoint = {
    logger.debug("get_json:" + reqJson)
    val JString(protoId) = reqJson \ "protoId"
    protoId match {
      case LOGIN_PROTO =>
        val LoginOrRegReq(username, password) = (reqJson \ "load").extract[LoginOrRegReq]

        //do database search
        val jsonRsp: JValue = {
          userSerivce.loginVerify(username, password) match {
            case Some(oid) => true
            case None => false
          }

        }

        RawEndPoint(Success(jsonRsp))
      case REGISTER_PROTO =>
        val LoginOrRegReq(username, password) = (reqJson \ "load").extract[LoginOrRegReq]

        //do database search
        val jsonRsp: JValue = {
          userSerivce.registerAccount(username, password) match {
            case Right(oid) =>
              ("status" -> 200) ~
              ("oid" -> oid.toString)
            case Left(error) =>
              ("status" -> 400) ~
              ("error" -> error)

          }

        }

        logger.debug(s"register_rsp: $jsonRsp")
        RawEndPoint(Success(jsonRsp))
      case undefined =>
        logger.warn(s"undefined_protoId: $undefined")
        EmptyEndPoint
    }
  }
}

case class LoginOrRegReq(userName: String, password: String)