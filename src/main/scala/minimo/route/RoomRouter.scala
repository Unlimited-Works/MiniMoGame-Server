package minimo.route

import lorance.rxsocket.presentation.json.{EndPoint, Router}
import org.json4s.JsonAST
import org.json4s.JsonAST.JString

/**
  *
  */
class RoomRouter extends Router {
  override val path: String = "room"

  override def apply(reqJson: JsonAST.JValue): EndPoint = {
    val JString(protoId) = reqJson \ "protoId"
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
