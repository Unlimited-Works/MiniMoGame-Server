package minimo.service.api

import minimo.service.api.Model.Room
import minimo.util.ObjectId

abstract class RoomService {

  def list: List[Room]

  def select(accountId: ObjectId, roomId: ObjectId): Boolean

  def create(accountId: ObjectId, roomName: String): ObjectId

  /**
    * delete the room if no one there
    *
    * @param roomId
    * @return true if delete the room
    */
  def leave(roomId: ObjectId): Boolean

  def find(roomId: ObjectId):Option[ObjectId]
}
