package minimo.rxsocket.serviceold

import scala.concurrent.Future

trait Service
/**
  *
  */
trait Module[-Req, +Rsp] extends (Req => Future[Rsp])
//
//trait RPCModule[Req, Rsp] extends Module[Req, Rsp] {
//  override def apply(v1: Req) = {
//
//  }
//}