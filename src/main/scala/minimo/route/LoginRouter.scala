package minimo.route

import minimo.exception.{BizCode, BizException}
import minimo.network.jsonsocket.{EmptyEndPoint, EndPoint, JRouter, RawEndPoint}
import minimo.network.jsession.MinimoSession
import minimo.service
import minimo.service.api.UserService
import minimo.util.ObjectId
import org.json4s.DefaultFormats
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.slf4j.LoggerFactory

import scala.util.Try

/**
  *
  */
class LoginRouter extends JRouter {
  import LoginRouter._

  private val logger = LoggerFactory.getLogger(getClass)
  implicit val formats: DefaultFormats.type = DefaultFormats

  val userSerivce: UserService = service.userService

  override val jsonPath = "login"

  override def jsonRoute(protoId: String, reqJson: JValue)(implicit session: MinimoSession): EndPoint = {
    logger.debug("get_json:" + reqJson)

    protoId match {
      case LOGIN_PROTO =>
        val LoginOrRegReq(username, password) = reqJson.extract[LoginOrRegReq]

        //do database search
        val jsonRsp: Try[JValue] = {
          Try(userSerivce.loginVerify(username, password)).map {
            case Some(oid) =>
              LoginRouter.getCurrentUserInfo match {
                case Some(_) => //让前一个用户强制退出
                case None => ()
              }
              // 这是当前用户已经登录

              LoginRouter.setCurrentUserInfo(UserInfo(oid, username, session.sessionId))
              JString(oid.toString)
            case None => JNull
          }

        }
        RawEndPoint.fromTry(jsonRsp)
      case REGISTER_PROTO =>
        val LoginOrRegReq(username, password) = reqJson.extract[LoginOrRegReq]

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
        RawEndPoint(jsonRsp)
      case undefined =>
        logger.warn(s"undefined_protoId: $undefined")
        EmptyEndPoint
    }
  }
}

object LoginRouter {
  case class LoginOrRegReq(userName: String, password: String)
  case class UserInfo(userId: ObjectId, userName: String, sessionId: String)

  def getCurrentUserInfo(implicit session: MinimoSession): Option[UserInfo] = {
    session.getData(data => {
      val userInfo = data.get("user_info")
      userInfo.map(_.asInstanceOf[UserInfo])
    })
  }

  def getCurrentUserInfoEx(implicit session: MinimoSession): UserInfo = {
    getCurrentUserInfo match {
      case None =>
        throw BizException(BizCode.SYSTEM_ERROR, "用户信息无法从session获取")
      case Some(rst) =>
        rst
    }
  }

  def setCurrentUserInfo(userInfo: UserInfo)(implicit session: MinimoSession): Boolean = {
    var isUpdated = false
    session.updateData(data => {
      val formerValue = data.put("user_info", userInfo)
      formerValue match {
        case None =>
          isUpdated = false
        case Some(_) =>
          isUpdated = true
      }
    })

    isUpdated
  }

}