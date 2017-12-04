package minimo.service

import minimo.dao.{ObjectId, RoomDao}
import minimo.service.api.LotteryService
import minimo.service.api.Model.Room

/**
  *
  */
class LotteryServiceImp extends LotteryService {
  override def listRooms: List[Room] = {
    RoomDao.listRooms
  }

  override def selectRoom(userId: ObjectId, roomId: ObjectId): Boolean = {
    RoomDao.getRoom(roomId) match {
      case None => false
      case Some(room) =>
        if (room.usersId.size == ServiceConfig.MAX_USERS_LIMIT) {
          false
        } else {
          RoomDao.addUserInRoom(userId, roomId)
          true
        }
    }
  }

  override def createRoom(accountId: ObjectId, roomName: String) = ???
}
