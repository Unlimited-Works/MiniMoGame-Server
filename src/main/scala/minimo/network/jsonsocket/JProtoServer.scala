package minimo.network.jsonsocket

import minimo.network.jsession.MinimoSession
import minimo.rxsocket.presentation.json.JProtocol
import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.json4s.JsonAST.{JString, JValue}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}
import org.slf4j.{Logger, LoggerFactory}

import collection.mutable.{Map => MutMap}
import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class JProtoServer(jProtos: Observable[JProtocol], routes: List[JRouter]) {
  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  val jRouterManager = new JRouterManager()

  jRouterManager.routes ++= routes.map(x => x.jsonPath -> x).toMap

  val protoWithSession: Observable[(JProtocol, MinimoSession)] = jProtos.map(jproto => {
    val sessionId = jproto.connectedSocket.addressPair.remote.toString //remote ip and port as sessionId
    val minimoSession = MinimoSession(sessionId, MutMap())
    (jproto, minimoSession)
  })

  //handle streams
  protoWithSession.subscribe { sktWithSession =>
    sktWithSession match {
      case (skt, session) =>
        implicit val minimoSession: MinimoSession = session
        JProtoServer.registerEvent(skt, routes)(session)

        skt.jRead.subscribe { jValue =>
          logger.info(s"get the socket json: {}", compact(render(jValue)))

          /**
            * protocol form client:
            * {
            *   taskId: ...
            *   load: {
            *     path: ...
            *     protoId: ...
            *     load: {
            *       ...`load-protocol`
            *     }
            *   }
            * }
            */
          val load = jValue \ "load"
          val taskId = jValue \ "taskId"
          val endPoint = jRouterManager.dispatch(load)

          /**
            * protocol to client in `load-protocol`:
            * {
            *   taskId: ...
            *   type: once/stream/error
            * *   // error type，表明这是非正常逻辑结果产生的异常，比如系统异常（这是有必要的，因为可能还没有返回EndPoint数据结构，就报错了，这种
            * *   错误在catch以后以error type返回）
            *   status: error/end/on
            *   load: {
            *     ...
            *   }
            * }
            */
          val rst: Future[Ack] = endPoint match {
            case raw @ RawEndPoint(_) =>
              JProtoServer.handleRawEndPoint(raw, taskId, skt)
            case fur @ FurEndPoint(_) =>
              JProtoServer.handleFurEndPoint(fur, taskId, skt)
            case stream @ StreamEndPoint(_) =>
              JProtoServer.handleStreamEndPoint(stream, taskId, skt)
            case rawAndStream @ RawAndStreamEndPoint(_, _) =>
              JProtoServer.handleRawAndStreamEndPoint(rawAndStream, taskId, skt)
            case EmptyEndPoint => ()
              Continue
            case ErrorEndPoint(code, desc, _ex) =>
              val finalJson = ("taskId" -> taskId) ~ ("type" -> "error") ~ ("status" -> "end") ~
                ("load" -> (("code" -> code) ~ ("desc" -> desc)))
              skt.sendRaw(finalJson).foreach(_ =>
                logger.info(s"send json: {}", compact(render(finalJson)))
              )
              Continue
          }
          rst
        }

        Continue
    }

  }

}

object JProtoServer {
  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  protected def handleRawEndPoint(rawEndPoint: RawEndPoint, taskId: JValue, skt: JProtocol) = {
    val jValRst = rawEndPoint.value
    val rst: Future[Ack] = jValRst match {
      case Failure(e) =>
        val finalJson =
          ("taskId" -> taskId) ~
            ("type" -> "once") ~
            ("status" -> "error") ~
            ("load" -> e.getMessage)
        skt.sendRaw(finalJson).flatMap(_ => {
          logger.info(s"send json: {}", compact(render(finalJson)))
          Continue
        })
      case Success(rst) =>
        val finalJson =
          ("taskId" -> taskId) ~
            ("type" -> "once") ~
            ("status" -> "end") ~
            ("load" -> rst)
        skt.sendRaw(finalJson).flatMap(_ => {
          logger.info(s"send json: {}", compact(render(finalJson)))
          Continue
        })
    }
    rst
  }

  /**
    * todo 什么才算一个stream准备好了呢？对于Future来说，为了保证消息的顺序性，可以等到Future完毕后再通知jprotocol完毕。
    * 但是Stream的准备事件并不确定。
    * @param streamEndPoint
    * @param taskId
    * @param skt
    * @return
    */
  protected def handleStreamEndPoint(streamEndPoint: StreamEndPoint, taskId: JValue, skt: JProtocol): Future[Ack] = {
    val jValRst = streamEndPoint.value
    val promise = Promise[Ack]
    jValRst.subscribe(
      event => {
        val finalJson: JValue =
          ("taskId" -> taskId) ~
            ("type" -> "stream") ~
            ("status" -> "on") ~
            ("load" -> event)

        //this stream i
        skt.sendRaw(finalJson).flatMap(_ => {
          logger.info(s"send json: {}", compact(render(finalJson)))
          Continue
        })
      },
      error => {
        logger.error("StreamEndPoint failed:", error)
        val finalJson: JValue =
          ("taskId" -> taskId) ~
            ("type" -> "stream") ~
            ("status" -> "error") ~
            ("load" -> error.getMessage)

        val rst: Future[Ack] = skt.sendRaw(finalJson).flatMap(_ => {
          logger.info(s"send json: {}", compact(render(finalJson)))
          Continue
        })

//        promise.completeWith(Continue)
        ()
      },
      () => {
        val finalJson: JValue =
          ("taskId" -> taskId) ~
            ("type" -> "stream") ~
            ("status" -> "end")

        skt.sendRaw(finalJson).foreach(_ =>
          logger.info(s"send json: {}", compact(render(finalJson)))
        )

//        promise.completeWith(Continue)
        ()
      }
    )
    // todo: completed main logic to completed the promise
//    promise.future
    Continue
  }

  def handleRawAndStreamEndPoint(rawAndStreamEndPoint: RawAndStreamEndPoint, taskId: JValue, skt: JProtocol): Future[Ack] = {
    val RawAndStreamEndPoint(rawEndPoint, streamEndPoint) = rawAndStreamEndPoint
    val JString(taskIdStr) = taskId
    val eventualAck = handleRawEndPoint(rawEndPoint, taskIdStr+":head", skt)
    val eventualAck1 = handleStreamEndPoint(streamEndPoint, taskIdStr+":stream", skt)
//    for {
//      r1 <- eventualAck
//      r2 <- eventualAck1
//    } yield {
//     r2
//    }

    eventualAck
  }

  protected def handleFurEndPoint(furEndPoint: FurEndPoint, taskId: JValue, skt: JProtocol): Future[Ack] = {
    val jValRstFur = furEndPoint.value
    val p = Promise[Unit]
    jValRstFur.map(jValRst => {
      val finalJson =
        ("taskId" -> taskId) ~
          ("type" -> "once") ~
          ("status" -> "end") ~
          ("load" -> jValRst)
      p.completeWith({
        logger.info(s"send json: {}", compact(render(finalJson)))
        skt.sendRaw(finalJson)
      })

    })
    jValRstFur.failed.map { error =>
      logger.error("FurEndPoint failed:", error)

      val finalJson =
        ("taskId" -> taskId) ~
          ("type" -> "once") ~
          ("status" -> "error") ~
          ("load" -> error.getMessage)
      p.completeWith({
        logger.info(s"send json: {}", compact(render(finalJson)))
        skt.sendRaw(finalJson)
      })
    }

    val rst: Future[Ack] = p.future.flatMap(_ => Continue)
    rst
  }

  //register event
  protected def registerEvent(skt: JProtocol, routes: List[JRouter])(implicit minimoSession: MinimoSession): Unit = {
    val onDisconnect = skt.connectedSocket.onDisconnected
    onDisconnect.onComplete(disconnectEvent => {
      logger.debug("disconnectEvent occurred")
      routes.map(router => {
        //1. 执行router触发事件
        try{
          router.onEvent(JProtoEvent.SocketDisconnect(disconnectEvent))
            .foreach(_compelted => {
              //2. 清除session
              MinimoSession.clear(minimoSession.sessionId)
            })
        } catch {
          case NonFatal(nf) =>
            logger.error("socket disconnect router Event fail: ", nf)
        }
      })
    })
  }
}