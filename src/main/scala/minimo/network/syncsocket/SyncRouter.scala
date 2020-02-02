package minimo.network.syncsocket

import minimo.network.jsession.MinimoSession

import scala.concurrent.Future

/**
  * 按照协议进行分发消息
  */
trait SyncRouter {
//  val syncProto: SyncProto

//  lazy val syncRegister: Unit = {
//    SyncRouter.routes += (syncProto -> this)
//  }

  def syncFn(data: SyncProto, session: MinimoSession): Future[Unit]

}

class SyncRouterManager(routes: Map[SyncProto, SyncRouter]) {
//  val routes = ()
//  import SyncProtoUnitImp._
  def dispatch(load: SyncProto, session: MinimoSession): Future[Unit] = {
    routes(load.unit).syncFn(load, session)
  }
}

