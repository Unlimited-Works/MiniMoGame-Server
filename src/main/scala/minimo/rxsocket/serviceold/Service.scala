//package minimo.rxsocket.service
//
//import rx.lang.scala.Observable
//
//import scala.concurrent.Future
//import scala.util.control.NonFatal
//
///**
//  * Module same as finagle's Service
//  */
//trait Service[-Req, +Rsp] extends (Req => Future[Rsp])
//
//trait Filter[-ReqIn, +RepOut, +ReqOut, -RepIn] extends ((ReqIn, Service[ReqOut, RepIn]) => Future[RepOut]) {
//  /**
//    * @return
//    *           (* Service *)
//    * [ReqIn -> (Req2 -> Rep2) -> RepOut]
//    *
//    */
//  def andThen[Req2, Rep2](next: Filter[ReqOut, RepIn, Req2, Rep2]) = new Filter[ReqIn, RepOut, Req2, Rep2] {
//    override def apply(request: ReqIn, module: Service[Req2, Rep2]) = {
//      val mdl: Service[ReqOut, RepIn] = new Service[ReqOut, RepIn] {
//        override def apply(v1: ReqOut) = {
//          try {
//            println("andthen filter")
//            next(v1, module)
//          } catch {
//            case NonFatal(e) => Future.failed(e)
//          }
//        }
//      }
//      Filter.this.apply(request, mdl)
//    }
//  }
//
//  def andThen(module: Service[ReqOut, RepIn]): Service[ReqIn, RepOut] = {
//    println("andthen module")
//    new Service[ReqIn, RepOut] {
//      def apply(request: ReqIn) = Filter.this.apply(request, module)
//    }
//  }
//}
//
//trait Module[Req, Rsp] extends Service[Observable[Req], Observable[Rsp]] {
//
//}
