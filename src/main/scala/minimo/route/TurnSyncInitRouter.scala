package minimo.route

import minimo.entity.{FrameEntity, RoomEntity}
import minimo.network.jsession.MinimoSession
import minimo.network.jsonsocket.{EndPoint, JRouter, RawAndStreamEndPoint, RawAndStreamValue, RawEndPoint, StreamEndPoint}
import minimo.route.TurnSyncInitRouter.FrameInitValue
import minimo.util.{JsonFormat, ObjectId}
import org.json4s.{Formats, JArray, JsonAST}
import org.slf4j.LoggerFactory

class TurnSyncInitRouter extends JRouter {

  private val logger = LoggerFactory.getLogger(getClass)

  override val jsonPath: String = "turn_sync_init"

  override def jsonRoute(protoId: String, load: JsonAST.JValue)(implicit session: MinimoSession): EndPoint = {
    protoId match {
      case TURN_SYNC_INIT_INIT_PROTO => // （为每个用户）初始化帧同步. 创建线程处理
        val roomEntity = LobbyRouter.sessionGetJoinedRoomEx
        val roomId = roomEntity.getRoomBaseInfo.roomId
        val frameEntity = FrameEntity.apply(roomId)

        TurnSyncInitRouter.sessionPutFrame(frameEntity)
        implicit val formats: Formats = JsonFormat.minimoFormats

        // return a observable
        RawAndStreamEndPoint(
          RawAndStreamValue(
            RawEndPoint.fromCaseClass(FrameInitValue(frameEntity.idFrame,
              frameEntity.systemPlayerId,
              frameEntity.currFrameCount)),
            StreamEndPoint.fromAny(frameEntity.frameStream)
          )
        )
    }
  }
}

object TurnSyncInitRouter {
  case class FrameInitValue(idFrame: ObjectId, systemPlayerId: ObjectId, currFrameCount: Long)



  def sessionPutFrame(frameEntity: FrameEntity)(implicit session: MinimoSession) = {
    session.updateData(data => {
      data.put("turn_sync:frame", frameEntity)
    })
  }

  def sessionGetFrame()(implicit session: MinimoSession): Option[FrameEntity] = {
    session.getData(data => {
      data.get("turn_sync:frame").map(_.asInstanceOf[FrameEntity])
    })
  }

  def sessionRemoveFrame()(implicit session: MinimoSession): Option[FrameEntity] = {
    session.getData(data => {
      data.remove("turn_sync:frame").map(_.asInstanceOf[FrameEntity])
    })
  }


  /**
    * 执行帧同步逻辑，主要是转发
    * @param session
    */
//  def turnSyncMessage(frameEntity: FrameEntity)(implicit session: MinimoSession): Unit = {
//    while (true) {
//      // do something
//
//
//      // break a frame time
//      Thread.sleep(1000 / 20)
//    }
//  }

}