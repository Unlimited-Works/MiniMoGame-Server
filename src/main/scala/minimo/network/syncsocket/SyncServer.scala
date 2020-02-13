package minimo.network.syncsocket

import minimo.rxsocket.session.{ConnectedSocket, ServerEntrance}
import monix.execution.Ack.Continue
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable

/**
  * SyncServer提供了一种紧凑的数据格式，用于对实时要求较高的场景中
  */
class SyncServer(host: String, port: Int, routers: Map[SyncProto, SyncRouter]) {

  val routerManager = new SyncRouterManager(routers)
//  routerManager.routes ++= routers

  val pos = new PositionSyncHandler()
  val initSessionHandler = new InitSessionSyncHandler()
  val syncParsers = Map(1.toShort -> pos, 0.toShort -> initSessionHandler)
  val syncServer: Observable[(ConnectedSocket[SyncProto], Observable[SyncProto])] =
    new ServerEntrance(host, port, () => new SyncParser(syncParsers))
      .listen
      .map(skt => skt -> skt.startReading)

  syncServer.subscribe(x => {
    x match {
      case (_, syncStream) =>

        SyncSubStatus(syncStream, routerManager).start

        //this logic refined by SubStatus
//        val sessionIdFur = Promise[String]
//        //init session status
//        syncStream.subscribe(y => {
//          y match {
//            case InitSessionProto(sessionID) =>
//              sessionIdFur.trySuccess(sessionID)
//          }
//
//          Stop
//        })
//
//        sessionIdFur.future.map(sessionId => {
//          syncStream.subscribe(syncProto => {
//            routerManager
//              .dispatch(syncProto, MinimoSession(sessionId, MutHash()))
//              .flatMap{_ => Continue}
//          })
//
//        })

    }

    Continue
  })

//  syncServer.subscribe( x => {
////    implicit val syncSktContext: ConnectedSocket[SyncProto] = x._1
//
//    x match {
//      case (_, syncStream) =>
//        syncStream.subscribe(syncProto => {
//          routerManager
//            .dispatch(syncProto)
//            .flatMap{_ => Continue}
//        })
//
//        Continue
//    }
//  })
}
