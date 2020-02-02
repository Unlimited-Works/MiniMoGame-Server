//package minimo.route
//
//import minimo.network.session.MinimoSession
//import minimo.network.jsonsocket.{EndPoint, JRouter}
//import org.json4s.JsonAST
//
///**
//  *
//  */
//class RoomRouter extends JRouter {
//  override val jsonPath: String = "room"
//
//  override def jsonRoute(protoId: String, reqJson: JsonAST.JValue, session: MinimoSession): EndPoint = {
//    protoId match {
//      case "PROTO_ROOM_CREATE" => ???
//      case "PROTO_ROOM_LEAVE" => ???
//      case "PROTO_ROOM_BEGIN" => ???
//      case "PROTO_ROOM_CHOICE_MAP" => ???
//      case "PROTO_ROOM_CHOICE_ROLE" => ???
//      case "PROTO_ROOM_READY" => ???
//      case "PROTO_ROOM_READY_CANCEL" => ???
//    }
//  }
//}
