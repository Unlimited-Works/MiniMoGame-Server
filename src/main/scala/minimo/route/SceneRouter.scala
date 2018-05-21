package minimo.route

import lorance.rxsocket.presentation.json.{EmptyEndPoint, EndPoint, JRouter}
import lorance.rxsocket.session.ConnectedSocket
import minimo.network.{PositionProto, SyncProto, SyncRouter}
import minimo.service.SceneService
import org.json4s.JsonAST
import org.json4s.JsonAST.JString

import scala.concurrent.{CanAwait, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Try

/**
  *
  */
class SceneRouter() extends SyncRouter with JRouter {

//  override val path: String = "scene"
//  override val syncProto: SyncProto = PositionProto(0,0,0)


  override def apply(reqJson: JsonAST.JValue): EndPoint = {
    val JString(protoId) = reqJson \ "protoId"
//    protoId match {
//      case "" =>
//        ???
//    }
    ???
  }

  override def syncFn(v1: SyncProto)(implicit syncSktContext: ConnectedSocket[SyncProto]): Future[Unit] = {
    v1 match {
      case pos: PositionProto =>
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