package minimo.network.syncsocket

import minimo.network.jsession.MinimoSession
import minimo.util.SubStatus
import monix.execution.Ack.Continue
import monix.reactive.Observable

import collection.mutable.{HashMap => MutHash}
import monix.execution.Scheduler.Implicits.global

/**
  *
  * @param source source of sync proto
  */
case class SyncSubStatus(source: Observable[SyncProto],
                         routerManager: SyncRouterManager) extends SubStatus[SyncProto] {
  var sessionId: String = _

  protected override def init: Status = {
    case InitSessionProto(sessionID) =>
      this.sessionId = sessionID
      //set session
      become(dispatch)
      Continue
  }

  def dispatch: Status = {
    case syncProto: SyncProto =>
      routerManager
        .dispatch(syncProto, MinimoSession(sessionId, MutHash()))
        .flatMap{_ => Continue}

  }

}