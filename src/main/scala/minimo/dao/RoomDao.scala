package minimo.dao

import minimo.util

/**
  *
  */
object RoomDao {
  import ctx._

  case class Rooms(oid: util.ObjectId, ownerId: String, usersId: List[util.ObjectId])

  def listRooms: List[Rooms] = {
    val q = quote {
      query[Rooms]
    }
    run(q)
  }

  def getRoom(roomId: util.ObjectId): Option[Rooms] = {
    val q = quote(
      query[Rooms].filter(room => room.oid == lift(roomId))
    )

    run(q).headOption
  }

  def addUserInRoom(userId: util.ObjectId, roomId: util.ObjectId): Long = {
    val uids: Seq[util.ObjectId] = Seq(userId)
    val v = quote(infix"(select array_agg(distinct e) from unnest(users_id || ${lift(uids)}) e)".as[List[util.ObjectId]])

    val q = quote {
      query[Rooms].filter(_.oid == lift(roomId))
        .update(_.usersId -> unquote(v))
    }

    ctx.run(q)
  }

}
