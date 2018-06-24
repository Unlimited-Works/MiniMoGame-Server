package minimo.network

import minimo.rxsocket.session.{ConnectedSocket, ServerEntrance}
import monix.execution.Ack.Continue
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global

/**
  * SyncServer提供了一种紧凑的数据格式
  */
class SyncServer(host: String, port: Int, routers: Map[SyncProto, SyncRouter]) {

  val routerManager = new SyncRouterManager(routers)
//  routerManager.routes ++= routers

  val pos = new PositionSyncHandler()
  val syncParsers = Map(1.toShort -> pos)
  val syncServer: Observable[(ConnectedSocket[SyncProto], Observable[SyncProto])] =
    new ServerEntrance(host, port, new SyncParser(syncParsers))
      .listen
      .map(skt => skt -> skt.startReading)

  syncServer.subscribe( x => {
    implicit val syncSktContext: ConnectedSocket[SyncProto] = x._1

    x match {
      case (_, syncStream) =>
        syncStream.subscribe(syncProto => {
          routerManager
            .dispatch(syncProto)
            .flatMap{_ => Continue}
        })

        Continue
    }
  })
}
