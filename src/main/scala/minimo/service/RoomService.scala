package minimo.service

import java.util.concurrent.ConcurrentHashMap

import minimo.entity.RoomEntity
import minimo.route.LoginRouter.UserInfo
import minimo.util.ObjectId
import monix.reactive.Observable
import monix.reactive.subjects.PublishSubject

import scala.collection.mutable

class RoomService {

}

object RoomService {
//  private val rooms = new ConcurrentHashMap[ObjectId, RoomEntity]()
//  private val roomsLock = new Object
//
//
//  def getRoomInfoById(roomId: String): Option[RoomEntity] = {
//    Option(rooms.get(roomId))
//  }
//
//  //todo use pages
//  def getAllRoomBaseInfo(): List[RoomBaseInfo] = {
//    val roomBaseInfos = mutable.ListBuffer[RoomBaseInfo]()
//    rooms.values().forEach(roomInfo => {
//      roomBaseInfos.addOne(RoomBaseInfo(roomInfo.roomId,
//        roomInfo.roomName,
//        roomInfo.roomUsers.size,
//        roomInfo.userCountLimit))
//    })
//    roomBaseInfos.toList
//  }



}