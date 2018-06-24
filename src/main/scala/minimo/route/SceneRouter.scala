package minimo.route

import minimo.rxsocket.presentation.json.{EndPoint, JRouter}
import minimo.module.Position
import minimo.network.{PositionProto, SyncProto, SyncRouter}
import minimo.service.PositionServiceImp
import org.json4s.JsonAST
import org.json4s.JsonAST.JString

import scala.concurrent.Future

/**
  *
  */
class SceneRouter() extends SyncRouter with JRouter {

  override val jsonPath: String = "scene"

  override def jsonRoute(protoId: String, reqJson: JsonAST.JValue): EndPoint = {
    ???
  }

  override def syncFn(v1: SyncProto): Future[Unit] = {
    v1 match {
      case pos: PositionProto =>
        // 保存位置到Cache中
        PositionServiceImp.setPos("1", Position(pos.x, pos.y, pos.z))


        //context to relative socket
//        val socket = implicitly[ConnectedSocket[SyncProto]]
//        val playerId = implicitly[PlayerId]
//        val sceneId = implicitly[SceneId]
//        val sceneServiceId = implicitly[SceneService]
//
//        sceneServiceId.syncPosition(playerId.id, SceneService.Position(pos.x, pos.y, pos.z))
        ???
    }
  }
}


object SceneRouter {
  case class PlayerId(id: String)
  case class SceneId(id: String)
}