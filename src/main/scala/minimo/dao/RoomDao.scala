package minimo.dao

/**
  *
  */
object RoomDao {
  import ctx._

  case class Rooms(oid: ObjectId, ownerId: String, usersId: List[ObjectId])

  def listRooms: List[Rooms] = {
    val q = quote {
      query[Rooms]
    }
    run(q)
  }

  def getRoom(roomId: ObjectId): Option[Rooms] = {
    val q = quote(
      query[Rooms].filter(room => room.oid == lift(roomId))
    )

    run(q).headOption
  }

  def addUserInRoom(userId: ObjectId, roomId: ObjectId): Long = {
    val uids: Seq[ObjectId] = Seq(userId)
    val v = quote(infix"(select array_agg(distinct e) from unnest(users_id || ${lift(uids)}) e)".as[List[ObjectId]])

    val q = quote {
      query[Rooms].filter(_.oid == lift(roomId))
        .update(_.usersId -> unquote(v))
    }

    ctx.run(q)
  }

}
