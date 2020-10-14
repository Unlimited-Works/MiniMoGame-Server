package minimo.route

import minimo.entity.RoomEntity
import minimo.entity.RoomEntity.{RoomUserInfo, RoomUsersAndJoinLeaveEvent}
import minimo.exception.{BizCode, BizException}
import minimo.network.jsession.MinimoSession
import minimo.network.jsonsocket.{EndPoint, JProtoEvent, JRouter, MultipleSessions, RawAndStreamEndPoint, RawAndStreamValue, RawEndPoint, StreamEndPoint}
import minimo.route.LoginRouter.UserInfo
import minimo.util.ObjectId
import org.json4s.Extraction._
import org.json4s.{JsonAST, _}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  *
  */
class LobbyRouter extends JRouter {
  private val logger = LoggerFactory.getLogger(getClass)

  //  private val rooms = new ConcurrentHashMap[String, mutable.ListBuffer[UserInfo]]()
  override val jsonPath: String = "lobby"

  override def jsonRoute(protoId: String, load: JsonAST.JValue)(implicit session: MinimoSession): EndPoint = {
    protoId match {
      case LOBBY_CREATE_ROOM_PROTO => //创建房间
        LobbyRouter.checkUserNotInRoomEx

        val JString(roomName) = load
        val currentUser = LoginRouter.getCurrentUserInfoEx
        val (roomEntity, roomUserInfo) = RoomEntity.apply(UserInfo(currentUser.userId, currentUser.userName, session.sessionId), roomName)
        assert(LobbyRouter.sessionPutJoinedRoom(roomEntity).isEmpty)
        RawEndPoint(decompose(roomUserInfo))

      case LOBBY_GET_ROOM_LIST_PROTO => //获取房间基本信息
        val jsonRoomInfos = RoomEntity.getAllRoomBaseInfo().map(roomBaseInfo => {
          decompose(roomBaseInfo)
        })
        RawEndPoint(JArray(jsonRoomInfos))

      case LOBBY_JOIN_ROOM_PROTO => //收到一个选择房间的消息
        LobbyRouter.checkUserNotInRoomEx

        val JString(roomId) = load
        val roomEntity = RoomEntity.getRoomInfoByIdEx(new ObjectId(roomId))
        val roomUserInfo = roomEntity.joinRoomEx(LoginRouter.getCurrentUserInfoEx)
        LobbyRouter.sessionPutJoinedRoom(roomEntity)

        RawEndPoint.fromCaseClass(roomUserInfo)
      case LOBBY_JOIN_ROOM_BY_NAME_PROTO => // 收到一个选择房间的消息（为了简化客户端流程） todo: use LOBBY_JOIN_ROOM_PROTO instead
        LobbyRouter.checkUserNotInRoomEx

        val JString(roomName) = load
        val roomEntity = RoomEntity.getRoomInfoByNameEx(roomName)
        val roomUserInfo = roomEntity.joinRoomEx(LoginRouter.getCurrentUserInfoEx)
        LobbyRouter.sessionPutJoinedRoom(roomEntity)

        RawEndPoint.fromCaseClass(roomUserInfo)
      case LOBBY_GET_ROOM_USERINFO_PROTO => //获取当前房间的用户信息, 返回一个stream
        val roomEntity: RoomEntity = LobbyRouter.sessionGetJoinedRoomEx
        val currentUserInfo = LoginRouter.getCurrentUserInfoEx
        val RoomUsersAndJoinLeaveEvent(roomUser, joinAndLeaveStream) =
                roomEntity.getRoomUserAndCreateEvent(currentUserInfo.userId)

        RawAndStreamEndPoint(
          RawAndStreamValue(
            RawEndPoint.fromCaseClass(roomUser),
            StreamEndPoint.fromAny(joinAndLeaveStream)
          )
        )

      case LOBBY_LIVE_ROOM_PROTO => //离开房间
        val roomEntity = LobbyRouter.sessionGetJoinedRoomEx
        val userInfo = LoginRouter.getCurrentUserInfoEx
        val rst = roomEntity.leaveRoomEx(userInfo)
        LobbyRouter.sessionDelJoinedRoom(roomEntity)
        RawEndPoint.fromCaseClass(rst)

      case LOBBY_START_GAME_PROTO => //游戏开始
        val roomEntity = LobbyRouter.sessionGetJoinedRoomEx

        //set room state
        val curUsers = roomEntity.startGame()

        RawEndPoint.fromCaseClass(curUsers, sendMode = MultipleSessions(Set.from(curUsers.map(_.userInfo.sessionId))))

    }

  }

  override def onEvent(event: JProtoEvent)(implicit minimoSession: MinimoSession): Future[Unit] = {
    event match {
      case JProtoEvent.SocketDisconnect(_) =>
        //退出房间
        logger.debug("onEvent leave room invoke, sessionGetJoinedRoom: " + LobbyRouter.sessionGetJoinedRoom)

        LobbyRouter.sessionGetJoinedRoom.foreach(currentRoom => {
          val userInfo = LoginRouter.getCurrentUserInfoEx
          val rst = currentRoom.leaveRoom(userInfo)
          logger.debug("onEvent leave room: " + rst)
        })

    }

    Future.successful(())
  }

  /**
    * 开始游戏.
    * 设置房间的状态（todo：房间的状态使用actor模型的become状态机控制比较合理。
    *               避免过多的flag变量，导致业务流程代码繁琐）
    */
//  private def startGame(implicit minimoSession: MinimoSession): EndPoint = {
//    //todo: check room state is satisfy begin game
//
//    //set room state
//    val roomEntity = LobbyRouter.sessionGetJoinedRoomEx
//    val curUsers = roomEntity.startGame()
//    RawEndPoint(curUsers)
//
//    //create a scene
//
//  }

}

/**
  * Lobby model
  */
object LobbyRouter {
  case class JoinedRoomInfo(roomId: String, roomName: String)
  case class CreateRoomReq(roomName: String)
  case class JoinAndLeaveEvent(joinEvent: UserInfo, leaveEvent: UserInfo)
  case class GameBeginRsp(netFrameCount: Long, phyFrameCount: Long, currUsers: List[RoomUserInfo])

//  case class GetRoomUserRsp(: String, roomName: String)

  //session 的key使用定义的case object，即方便查找，也比enum方便扩展
  def sessionPutJoinedRoom(room: RoomEntity)(implicit session: MinimoSession) = {
    session.updateData(data => {
      data.put("joined_room_info", room)
    })
  }

  def sessionDelJoinedRoom(room: RoomEntity)(implicit session: MinimoSession) = {
    session.updateData(data => {
      data.remove("joined_room_info")
    })
  }


  def sessionGetJoinedRoom(implicit session: MinimoSession): Option[RoomEntity] = {
    session.getData(data => {
      val joinedRoomInfo = data.get("joined_room_info")
      joinedRoomInfo.map(_.asInstanceOf[RoomEntity])
    })
  }

  def sessionGetJoinedRoomEx(implicit session: MinimoSession): RoomEntity = {
    sessionGetJoinedRoom match {
      case None =>
        throw new BizException(BizCode.SYSTEM_ERROR, "用户不在该房间中")
      case Some(value) =>
        value
    }
  }

  def checkUserNotInRoomEx(implicit session: MinimoSession): Unit = {
    if(LobbyRouter.sessionGetJoinedRoom.nonEmpty) {
      throw BizException(BizCode.LOGIC_FAIL, "该用户已经加入房间")
    }
  }
}