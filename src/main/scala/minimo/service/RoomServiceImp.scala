//package minimo.service
//
//import minimo.dao.RoomDao
//import minimo.service.api.RoomService
//import minimo.service.api.Model.Room
//import minimo.util.ObjectId
//
///**
//  *
//  */
//class RoomServiceImp extends RoomService {
//  override def list: List[Room] = {
//    RoomDao.listRooms
//  }
//
//  override def select(userId: ObjectId, roomId: ObjectId): Boolean = {
//    RoomDao.getRoom(roomId) match {
//      case None => false
//      case Some(room) =>
//        if (room.usersId.size == ServiceConfig.MAX_USERS_LIMIT) {
//          false
//        } else {
//          RoomDao.addUserInRoom(userId, roomId)
//          true
//        }
//    }
//  }
//
//  override def create(accountId: ObjectId, roomName: String) = ???
//
//  override def leave(roomId: ObjectId): Boolean = ???
//
//  override def find(roomId: ObjectId): Option[ObjectId] = ???
//}
