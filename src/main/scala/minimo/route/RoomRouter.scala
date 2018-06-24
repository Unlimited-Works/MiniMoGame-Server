package minimo.route

import minimo.rxsocket.presentation.json.{EndPoint, JRouter}
import org.json4s.JsonAST
import org.json4s.JsonAST.JString

/**
  *
  */
class RoomRouter extends JRouter {
  override val jsonPath: String = "room"

  override def jsonRoute(protoId: String, reqJson: JsonAST.JValue): EndPoint = {
    protoId match {
      case "PROTO_ROOM_CREATE" => ???
      case "PROTO_ROOM_LEAVE" => ???
      case "PROTO_ROOM_BEGIN" => ???
      case "PROTO_ROOM_CHOICE_MAP" => ???
      case "PROTO_ROOM_CHOICE_ROLE" => ???
      case "PROTO_ROOM_READY" => ???
      case "PROTO_ROOM_READY_CANCEL" => ???
    }
  }
}
