package minimo.entity

import java.util.concurrent.ConcurrentHashMap

import minimo.entity.RoomEntity._
import minimo.exception.{BizCode, BizException}
import minimo.route.LoginRouter.UserInfo
import minimo.util.ObjectId
import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import monix.reactive.subjects.{PublishSubject, PublishToOneSubject, ReplaySubject}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.observables.ConnectableObservable

import scala.collection.mutable


class RoomEntity(
                val roomId: ObjectId,
                var roomName: String,
                var owner: UserInfo,
                var roomStatus: RoomStatus.Value,
                val roomUsers: mutable.Set[RoomUserInfo],
                var userCountLimit: Int = 8,//user can change the limit
               ) {
  //properties
  private val joinRoomSub = PublishSubject[RoomUserInfo]()
  joinRoomSub.subscribe()
  val joinRoomStream: Observable[RoomUserInfo] = joinRoomSub

  private val leaveRoomSub = PublishSubject[RoomUserInfo]()
  leaveRoomSub.subscribe()
  val leaveRoomStream: Observable[RoomUserInfo] = leaveRoomSub

  //todo fix bug: lost some hot message
  def getRoomUserAndEvent: RoomUsersAndJoinLeaveEvent = roomId.synchronized{
    //todo create a multicast stream: cache event util first subscribe message( todo: consider performance)
    val rstStream = PublishToOneSubject[JoinAndLeaveEvent]()
    joinRoomSub.map(x => JoinAndLeaveEvent(true, x)).subscribe(rstStream)
    leaveRoomSub.map(x => JoinAndLeaveEvent(false, x)).subscribe(rstStream)

    RoomUsersAndJoinLeaveEvent(roomUsers.toList, rstStream)

  }

  /**
    *
    * @param userInfo
    * @return true 加入成功 false加入失败
    */
  def joinRoom(userInfo: UserInfo): Either[RoomFailResult.Value, RoomUserInfo] = this.roomId.synchronized {
    assert(this.roomStatus == RoomStatus.OPEN)
    val users = this.roomUsers
    users.exists(_.userInfo.userId == userInfo.userId) match {
      case true => Left(RoomFailResult.user_has_exist)//已经存在，拒绝加入
      case false =>
        var newIndex: Int = 0
        users.foreach(roomInfo => {
          if(roomInfo.index > newIndex) newIndex = roomInfo.index
        })
        if(newIndex < this.userCountLimit) {//可以加入用户
          val joinedUserInfo = RoomUserInfo(newIndex+1, userInfo)
          users.addOne(joinedUserInfo)

          joinRoomSub.onNext(joinedUserInfo)
          Right(joinedUserInfo)
        } else {//人数已满, 拒绝加入
          Left(RoomFailResult.room_has_full)
        }

    }
  }

  def joinRoomEx(userInfo: UserInfo): RoomUserInfo = {
    joinRoom(userInfo) match {
      case Left(value) =>
        //todo: Enum only support int and String. replace Enum with custom (code: String, desc: String) case class
        throw BizException(value.toString, "加入房间失败")
      case rst @ Right(joinedUserInfo) =>
        joinedUserInfo
    }
  }

  def startGame(): Unit = {
    this.roomId.synchronized {
      this.roomStatus = RoomStatus.GAMING
    }
  }

  def leaveRoom(userInfo: UserInfo): Either[RoomEntity.RoomFailResult.Value, LeaveRoomRst] =
                                                                this.roomId.synchronized{
    this.roomUsers.find(_.userInfo.userId.equals(userInfo.userId)) match {
      case None =>
        Left(RoomFailResult.user_not_exist)
      case Some(userInfo) =>
        leaveRoom(userInfo)
    }

  }

  def leaveRoomEx(userInfo: UserInfo): LeaveRoomRst = this.roomId.synchronized {
    leaveRoom(userInfo) match {
      case Left(fail) =>
        throw BizException(fail.toString, "离开房间失败")
      case Right(rst) =>
        rst
    }

  }

  /**
    * room's reference is managed by RoomService, the room ref is managed rooms.
    * @return Right Some(userInfo): new room owner
    *         Right None: not set new owner
    */
  def leaveRoom(roomUserInfo: RoomUserInfo): Either[RoomFailResult.Value, LeaveRoomRst] =
                                                                this.roomId.synchronized {
    assert(this.roomStatus == RoomStatus.OPEN)
    val user = roomUserInfo.userInfo
    val rst = if(this.owner.userId == user.userId) { //当前用户为房主,将房主转移给另外一个人
      val movedCurrentUser = this.roomUsers.remove(roomUserInfo)
      val secondUserOpt = this.roomUsers.headOption
      secondUserOpt match {
        case None =>
          //已经没有用户了，删除房间
          RoomEntity.delRoom(this) match {
            case true =>
              Right(LeaveRoomRst(roomRemoved = true, generateNewOwner = false, None)) //??怎么返回呢
            case false => Left(RoomFailResult.room_not_exist)
          }
        case Some(secondUserInfo) =>
          //存在第二个用户，房主移交给他
          this.owner = secondUserInfo.userInfo
          Right(LeaveRoomRst(false, true, Some(secondUserInfo.userInfo)))
      }
    } else {//当前用户不为房主，移除该用户
      val removed = this.roomUsers.remove(roomUserInfo)
      removed match {
        case true =>
          Right(LeaveRoomRst(false, false, None))
        case false =>
          Left(RoomFailResult.user_not_exist)
      }
    }

    //event emit
    if(rst.isRight) {
      leaveRoomSub.onNext(roomUserInfo)
    }
    rst
  }

  def getRoomBaseInfo: RoomBaseInfo = {
    RoomBaseInfo(roomId, roomName, roomUsers.size, userCountLimit)
  }

}

object RoomEntity {

  //data
  private val rooms = new ConcurrentHashMap[ObjectId, RoomEntity]()
  private val roomsLock = new Object

  //construct
  def apply(creator: UserInfo, roomName: String): (RoomEntity, RoomUserInfo) = {
    val roomId = new ObjectId()
    val roomUserInfo = RoomUserInfo(0, creator)
    val roomInfo = new RoomEntity(
      roomId,
      roomName,
      creator,
      RoomStatus.OPEN,
      mutable.Set[RoomUserInfo]().addOne(roomUserInfo)
    )

    rooms.put(roomId, roomInfo)
    (roomInfo, roomUserInfo)
  }

  //public method

  def getRoomInfoById(roomId: ObjectId): Option[RoomEntity] = {
    Option(rooms.get(roomId))
  }

  def getRoomInfoByIdEx(roomId: ObjectId): RoomEntity = {
    Option(rooms.get(roomId)) match {
      case None =>
        throw BizException(BizCode.LOGIC_FAIL, "房间不存在")
      case Some(roomEntity) =>
        roomEntity
    }
  }

  def getRoomInfoByName(roomName: String): Option[RoomEntity] = {
    var room: Option[RoomEntity] = None
    rooms.values().forEach(a => {
      if(a.roomName == roomName) {
        room = Some(a)
      }
    })
    room

  }

  def getRoomInfoByNameEx(roomName: String): RoomEntity = {
    getRoomInfoByName(roomName) match {
      case None =>
        throw BizException(BizCode.LOGIC_FAIL, "房间不存在")
      case Some(roomEntity) =>
        roomEntity
    }
  }

  //todo use pages
  def getAllRoomBaseInfo(): List[RoomBaseInfo] = {
    val roomBaseInfos = mutable.ListBuffer[RoomBaseInfo]()
    rooms.values().forEach(roomInfo => {
      roomBaseInfos.addOne(RoomBaseInfo(roomInfo.roomId,
        roomInfo.roomName,
        roomInfo.roomUsers.size,
        roomInfo.userCountLimit))
    })
    roomBaseInfos.toList
  }

  //private method
  private[RoomEntity] def delRoom(roomInfo: RoomEntity): Boolean = {
    val value = RoomEntity.rooms.remove(roomInfo.roomId)
    if(value == null) false else true
  }

  // data structure
  object RoomStatus extends Enumeration {
    val OPEN = Value(1)
    val GAMING = Value(2)
  }

  case class RoomUserInfo(index: Int, userInfo: UserInfo)

  case class RoomBaseInfo(roomId: ObjectId,
                          roomName: String,
                          currentUserCount: Int,
                          userCountLimit: Int)

  object RoomFailResult extends Enumeration {
    val user_has_exist = Value("room:user_has_exist")
    val room_has_full = Value("room:room_has_full")
    val room_not_exist = Value("room:room_not_exist")
    val user_not_exist = Value("room:user_not_exist")
  }
  case class LeaveRoomRst(roomRemoved: Boolean, generateNewOwner: Boolean, newOwnerOpt: Option[UserInfo])

  case class RoomUsersAndJoinLeaveEvent(currentUser: List[RoomUserInfo], joinAndLeaveEvent: Observable[JoinAndLeaveEvent])
  case class JoinAndLeaveEvent(isJoinEvent: Boolean, roomUserInfo: RoomUserInfo)


}