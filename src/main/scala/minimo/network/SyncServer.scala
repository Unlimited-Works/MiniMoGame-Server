package minimo.network

import java.util.concurrent.ConcurrentHashMap

import lorance.rxsocket.presentation.json.JRouter
import lorance.rxsocket.session.{ConnectedSocket, ServerEntrance}
import minimo.service.SceneService
import monix.execution.Ack.Continue
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global

class SyncServer(host: String, port: Int, routers: Map[SyncProto, SyncRouter]) {

  val pos = new PositionSyncHandler()
  val routerManager = new SyncRouterManager()
  val syncParsers = Map(1.toShort -> pos)

  val syncServer: Observable[(ConnectedSocket[SyncProto], Observable[SyncProto])] =
    new ServerEntrance(host, port, new SyncParser(syncParsers))
      .listen
      .map(skt => skt -> skt.startReading)

//  val socketNameMap = new ConcurrentHashMap[ConnectedSocket[SyncProto]]()

  syncServer.subscribe( x => {
//    socketNameMap.put("", x._1)
    implicit val syncSktContext: ConnectedSocket[SyncProto] = x._1

    x match {
      case (_, syncStream) =>
        syncStream.subscribe(syncProto => {
          routerManager.dispatch(syncProto).flatMap(_ => Continue)
        })

        Continue
    }
  })
}
