package minimo.service.api

import minimo.service.api.Model.Room
import minimo.util.ObjectId

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

