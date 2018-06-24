package minimo.network

import minimo.rxsocket.session.ConnectedSocket
import scala.concurrent.Future

/**
  * 按照协议进行分发消息
  */
trait SyncRouter {
//  val syncProto: SyncProto

//  lazy val syncRegister: Unit = {
//    SyncRouter.routes += (syncProto -> this)
//  }

  def syncFn(data: SyncProto): Future[Unit]

}

class SyncRouterManager(routes: Map[SyncProto, SyncRouter]) {
//  val routes = ()
//  import SyncProtoUnitImp._
  def dispatch(load: SyncProto): Future[Unit] = {
    routes(load.unit).syncFn(load)
  }
}

