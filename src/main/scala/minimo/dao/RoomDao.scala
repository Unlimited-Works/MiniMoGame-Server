package minimo.dao

/**
  *
  */
object RoomDao {
  import ctx._

  case class Room(oid: ObjectId, owner: String, users: List[ObjectId])

  def listRooms: List[Room] = {
    val q = quote {
      query[Room]
    }
    run(q)
  }

  def getRoom(roomId: ObjectId): Option[Room] = {
    val q = quote(
      query[Room].filter(room => room.oid == lift(roomId))
    )

    run(q).headOption
  }

  def addUserInRoom(userId: ObjectId): Unit = {
    val q = quote(
      query[Room].update(room => room.users -> {userId :: room.users distinct})
    )

    run(q)
  }

}
