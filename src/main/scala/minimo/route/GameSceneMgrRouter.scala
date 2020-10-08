package minimo.route

import minimo.entity.FrameEntity
import minimo.network.jsession.MinimoSession
import minimo.network.jsonsocket.{EmptyEndPoint, EndPoint, JRouter, RawEndPoint}
import org.json4s.JsonAST
import org.slf4j.LoggerFactory

/**
  * 处理游戏场景中的帧同步数据
  */
class GameSceneMgrRouter extends JRouter {

  private val logger = LoggerFactory.getLogger(getClass)

  //  private val rooms = new ConcurrentHashMap[String, mutable.ListBuffer[UserInfo]]()
  override val jsonPath: String = "game_scene_mgr"

  override def jsonRoute(protoId: String, load: JsonAST.JValue)(implicit session: MinimoSession): EndPoint = {
    protoId match {
      case TURN_SYNC_END_PROTO =>
        val frameEntity: FrameEntity = TurnSyncInitRouter.sessionGetFrame().get
        frameEntity.closeGame()
        TurnSyncInitRouter.sessionRemoveFrame()
        RawEndPoint(JsonAST.JNull)
    }
  }
}

