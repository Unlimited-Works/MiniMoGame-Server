package minimo.route

import minimo.entity.FrameEntity
import minimo.network.jsession.MinimoSession
import minimo.network.jsonsocket.{EmptyEndPoint, EndPoint, JRouter, RawEndPoint}
import org.json4s.JsonAST
import org.json4s.JsonAST.JBool
import org.slf4j.LoggerFactory

/**
  * 处理游戏场景中的帧同步数据
  */
class TurnSyncRouter extends JRouter {

  private val logger = LoggerFactory.getLogger(getClass)

  //  private val rooms = new ConcurrentHashMap[String, mutable.ListBuffer[UserInfo]]()
  override val jsonPath: String = "turn_sync"

  override def jsonRoute(protoId: String, load: JsonAST.JValue)(implicit session: MinimoSession): EndPoint = {
    protoId match {
      case TURN_SYNC_INPUT_PROTO => //输入信息同步（后续要做限流，这里相信客户端按照频率发送）
        val inputKey = load.extract[FrameEntity.Cmd]

        val frameEntity: FrameEntity = TurnSyncInitRouter.sessionGetFrame().get
        val userInfo = LoginRouter.getCurrentUserInfoEx
        frameEntity.putCurFrame(userInfo.userId, List(inputKey))

        EmptyEndPoint
      // todo end proto should be active push to client
      case TURN_SYNC_END_PROTO =>
        val frameEntity: FrameEntity = TurnSyncInitRouter.sessionGetFrame().get
        frameEntity.closeGame()
        TurnSyncInitRouter.sessionRemoveFrame()
        RawEndPoint(JsonAST.JNull)
    }
  }
}

object TurnSyncRouter {
}