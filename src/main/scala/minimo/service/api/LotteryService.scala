package minimo.service.api

import minimo.dao.ObjectId
import minimo.service.api.Model.Room

/**
  *
  */
trait LotteryService {

  /**
    * get all rooms
    * @return
    */
  def listRooms: List[Room]

  def selectRoom(accountId: ObjectId, roomId: ObjectId): Boolean

  def createRoom(accountId: ObjectId, roomName: String): ObjectId
}

