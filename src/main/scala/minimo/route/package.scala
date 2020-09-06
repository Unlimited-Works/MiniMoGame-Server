package minimo
import minimo.util.JsonFormat
import org.json4s.Formats

/**
  * Router package deal with client usage relative.
  * Include session, JProto data convert to Scala class
  */
package object route {
  val LOGIN_PROTO = "LOGIN_PROTO"
  val REGISTER_PROTO = "REGISTER_PROTO"

  //lobby
  val LOBBY_CREATE_ROOM_PROTO="LOBBY_CREATE_ROOM_PROTO"
  val LOBBY_GET_ROOM_LIST_PROTO="LOBBY_GET_ROOM_LIST_PROTO"
  val LOBBY_JOIN_ROOM_PROTO="LOBBY_SELECT_ROOM_PROTO"
  val LOBBY_JOIN_ROOM_BY_NAME_PROTO = "LOBBY_JOIN_ROOM_BY_NAME_PROTO"
  val LOBBY_GET_ROOM_USERINFO_PROTO="LOBBY_GET_ROOM_USERINFO_PROTO"
  val LOBBY_START_GAME_PROTO="LOBBY_START_GAME_PROTO"
  val LOBBY_LIVE_ROOM_PROTO="LOBBY_LIVE_ROOM_PROTO"
//  val SYNC_POSITION_PROTO = "SYNC_POSITION_PROTO"


  // Turn Sync Init
  val TURN_SYNC_INIT_INIT_PROTO = "TURN_SYNC_INIT_INIT_PROTO"


  // turn sync
  val TURN_SYNC_INPUT_PROTO = "TURN_SYNC_INPUT_PROTO"

  implicit val formats: Formats = JsonFormat.minimoFormats

}
