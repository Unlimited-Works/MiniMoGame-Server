package minimo.network

import lorance.rxsocket.session.ConnectedSocket
import scala.concurrent.Future

/**
  * 按照协议进行分发消息
  */
trait SyncRouter {
//  val syncProto: SyncProto

//  lazy val syncRegister: Unit = {
//    SyncRouter.routes += (syncProto -> this)
//  }

  def syncFn(data: SyncProto)(implicit syncSktContext: ConnectedSocket[SyncProto]): Future[Unit]

}

class SyncRouterManager {
  val routes = collection.mutable.HashMap[SyncProto, SyncRouter]()

  def dispatch(load: SyncProto)(implicit syncSktContext: ConnectedSocket[SyncProto]): Future[Unit] = {
    load match {
      case pos: PositionProto =>
        routes(pos).syncFn(load)
    }
  }
}

