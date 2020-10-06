package minimo.network.jsonsocket

import java.util.concurrent.ConcurrentHashMap

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
  protected val sessionSkt = new ConcurrentHashMap[String, JProtocol]()

  val jRouterManager = new JRouterManager()

  jRouterManager.routes ++= routes.map(x => x.jsonPath -> x).toMap

  val protoWithSession: Observable[(JProtocol, MinimoSession)] = jProtos.map(jproto => {
    val sessionId = jproto.connectedSocket.addressPair.remote.toString //remote ip and port as sessionId
    val minimoSession = MinimoSession(sessionId, MutMap())
    this.sessionSkt.put(sessionId, jproto)
    (jproto, minimoSession)
  })

  //handle streams
  protoWithSession.subscribe { sktWithSession =>
    sktWithSession match {
      case (skt, session) =>
        implicit val minimoSession: MinimoSession = session
        this.registerEvent(skt, routes)(session)

        skt.jRead.subscribe { jValue =>
          logger.info(s"get the socket json: {}", compact(render(jValue)))

          /**
            * protocol form client:
            * {
            *   taskId: ...
            *   path: ...
            *   protoId: ...
            *   load: {
            *       ...
            *   }
            * }
            */
          val load = jValue \ "load"
          val taskId = jValue \ "taskId"
          val JString(path) = jValue \ "path"
          val JString(protoId) = jValue \ "protoId"
          val endPoint = jRouterManager.dispatch(path, protoId, load)

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
          val rst: Future[Ack] = this.handleEndPoint(skt, endPoint, taskId, path, protoId)
          rst
        }

        Continue
    }

  }

  private def handleEndPoint(skt: JProtocol, endPoint: EndPoint, taskId: JValue, path: String, protoId: String): Future[Ack] = {
    endPoint match {
      case raw @ RawEndPoint(value, sendMode) =>
        this.handleRawEndPoint(raw, taskId, skt, sendMode, path, protoId)
      case fur @ FurEndPoint(value, sendMode) =>
        this.handleFurEndPoint(fur, taskId, skt, sendMode, path, protoId)
      case stream @ StreamEndPoint(value, sendMode) =>
        this.handleStreamEndPoint(stream, taskId, skt, sendMode, path, protoId)
      case rawAndStream @ RawAndStreamEndPoint(RawAndStreamValue(raw, stream), sendMode) =>
        this.handleRawAndStreamEndPoint(rawAndStream, taskId, skt, sendMode, path, protoId)
      case EmptyEndPoint => ()
        Continue
      case ErrorEndPoint(ErrorValue(code, desc, ex), sendMode) =>
        val finalJson = ("taskId" -> taskId) ~ ("path" -> path) ~("protoId" -> protoId) ~ ("type" -> "error") ~ ("status" -> "end") ~
          ("load" -> (("code" -> code) ~ ("desc" -> desc)))
        this.sendJsonRsp(finalJson, skt, sendMode).foreach(_ =>
          logger.info(s"send json: {}", compact(render(finalJson)))
        )
        Continue
    }
  }

  private def handleRawEndPoint(rawEndPoint: RawEndPoint, taskId: JValue, skt: JProtocol, sendMode: SendMode, path: String, protoId: String) = {
    val jValRst = rawEndPoint.value
    val rst: Future[Ack] = jValRst match {
      case Failure(e) =>
        val finalJson =
          ("taskId" -> taskId) ~
            ("path" -> path) ~ ("protoId" -> protoId) ~
            ("type" -> "once") ~
            ("status" -> "error") ~
            ("load" -> e.getMessage)
        this.sendJsonRsp(finalJson, skt, sendMode).flatMap(_ => {
          logger.info(s"send json: {}", compact(render(finalJson)))
          Continue
        })
      case Success(rst) =>
        val finalJson =
          ("taskId" -> taskId) ~
            ("path" -> path) ~ ("protoId" -> protoId) ~
            ("type" -> "once") ~
            ("status" -> "end") ~
            ("load" -> rst)
        this.sendJsonRsp(finalJson, skt, sendMode).flatMap(_ => {
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
  private def handleStreamEndPoint(streamEndPoint: StreamEndPoint, taskId: JValue, skt: JProtocol, sendMode: SendMode, path: String, protoId: String): Future[Ack] = {
    val jValRst = streamEndPoint.value
    val promise = Promise[Ack]
    jValRst.subscribe(
      event => {
        val finalJson: JValue =
          ("taskId" -> taskId) ~
            ("path" -> path) ~ ("protoId" -> protoId) ~
            ("type" -> "stream") ~
            ("status" -> "on") ~
            ("load" -> event)

        //this stream i
        this.sendJsonRsp(finalJson, skt, sendMode).flatMap(_ => {
          logger.info(s"send json: {}", compact(render(finalJson)))
          Continue
        })
      },
      error => {
        logger.error("StreamEndPoint failed:", error)
        val finalJson: JValue =
          ("taskId" -> taskId) ~
            ("path" -> path) ~ ("protoId" -> protoId) ~
            ("type" -> "stream") ~
            ("status" -> "error") ~
            ("load" -> error.getMessage)

        val rst: Future[Ack] = this.sendJsonRsp(finalJson, skt, sendMode).flatMap(_ => {
          logger.info(s"send json: {}", compact(render(finalJson)))
          Continue
        })

//        promise.completeWith(Continue)
        ()
      },
      () => {
        val finalJson: JValue =
          ("taskId" -> taskId) ~
            ("path" -> path) ~ ("protoId" -> protoId) ~
            ("type" -> "stream") ~
            ("status" -> "end")

        this.sendJsonRsp(finalJson, skt, sendMode).foreach(_ =>
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

  private def handleRawAndStreamEndPoint(rawAndStreamEndPoint: RawAndStreamEndPoint, taskId: JValue, skt: JProtocol, sendMode: SendMode, path: String, protoId: String): Future[Ack] = {
    val RawAndStreamValue(rawEndPoint, streamEndPoint) = rawAndStreamEndPoint.value
    val JString(taskIdStr) = taskId
    val eventualAck = handleRawEndPoint(rawEndPoint, taskIdStr+":head", skt, sendMode, path, protoId)
    val eventualAck1 = handleStreamEndPoint(streamEndPoint, taskIdStr+":stream", skt, sendMode, path, protoId)
    eventualAck
  }

  private def handleFurEndPoint(furEndPoint: FurEndPoint, taskId: JValue, skt: JProtocol, sendMode: SendMode, path: String, protoId: String): Future[Ack] = {
    val jValRstFur = furEndPoint.value
    val p = Promise[Unit]
    jValRstFur.map(jValRst => {
      val finalJson =
        ("taskId" -> taskId) ~
          ("path" -> path) ~ ("protoId" -> protoId) ~
          ("type" -> "once") ~
          ("status" -> "end") ~
          ("load" -> jValRst)
      p.completeWith({
        logger.info(s"send json: {}", compact(render(finalJson)))
        this.sendJsonRsp(finalJson, skt, sendMode)
      })

    })
    jValRstFur.failed.map { error =>
      logger.error("FurEndPoint failed:", error)

      val finalJson =
        ("taskId" -> taskId) ~
          ("path" -> path) ~ ("protoId" -> protoId) ~
          ("type" -> "once") ~
          ("status" -> "error") ~
          ("load" -> error.getMessage)
      p.completeWith({
        logger.info(s"send json: {}", compact(render(finalJson)))
        this.sendJsonRsp(finalJson, skt, sendMode)
      })
    }

    val rst: Future[Ack] = p.future.flatMap(_ => Continue)
    rst
  }

  //register event
  private def registerEvent(skt: JProtocol, routes: List[JRouter])(implicit minimoSession: MinimoSession): Unit = {
    val onDisconnect = skt.connectedSocket.onDisconnected
    onDisconnect.onComplete(disconnectEvent => {
      logger.debug("disconnectEvent occurred")
      routes.foreach(router => {
        //1. 执行router触发事件
        try{
          router.onEvent(JProtoEvent.SocketDisconnect(disconnectEvent))
            .foreach(_ => {
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

  private def sendJsonRsp(value: JValue, selfProto: JProtocol, sendMode: SendMode): Future[Unit] = {
    val jprotos = this.selectUserByMode(selfProto, sendMode)
    val rst = jprotos.map(item => item.sendRaw(value))
    Future.sequence(rst).map(_ => Future.successful())
  }

  private def selectUserByMode(selfProto: JProtocol, sendMode: SendMode): Set[JProtocol] = {
    sendMode match {
      case Non => Set.empty
      case a @ Self => Set(selfProto)
      case a @ Zone(zoneId) => ???
      case Zones(zoneIds, excludeSessions, excludeSelf) => ???
      case MultipleSessions(sessionIds) =>
        val rst = sessionIds.flatMap { s => {
          Option(this.sessionSkt.get(s))
        }}



        rst
    }
  }

}