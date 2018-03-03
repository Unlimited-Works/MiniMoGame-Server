package rxsocket.demo

import org.slf4j.LoggerFactory
import lorance.rxsocket.presentation.json.JProtocol
import lorance.rxsocket.session.ClientEntrance
import monix.execution.Ack.Continue
import monix.reactive.Observable

import scala.concurrent.{Future, Promise}
import monix.execution.Scheduler.Implicits.global

/**
  * Json presentation Example
  */
object JProtoClient extends App {
  private val logger = LoggerFactory.getLogger(getClass)
  val client = new ClientEntrance("localhost", 10012).connect
  val jproto = client.map { x => new JProtocol(x, x.startReading) }

  val namesFur = getMyNames("admin")

  namesFur.foreach(names => logger.info(names.toString))

  Thread.currentThread().join()

// older api `sendWithResult`
//  def getMyNames(accountId: String) = {
//    jproto.flatMap { s =>
//      val rsp = s.sendWithResult[Response, Request](Request(accountId),
//        Some((x: Observable[Response]) => x.takeWhile(_.result.nonEmpty))//`takeWhile` marks which point the stream completed
//      )
//      toFuture(rsp)
//    }
//  }

  def getMyNames(accountId: String) = {
    jproto.flatMap { s =>
      val rsp = s.sendWithStream[Request, Response](Request(accountId),
        Some((x: Observable[Response]) => x.takeWhile(_.result.nonEmpty))//`takeWhile` marks which point the stream completed
      )
      toFuture(rsp)
    }
  }

  //transfer stream to Future if need
  private def toFuture(observable: Observable[Response]): Future[List[Response]] = {
    val p = Promise[List[Response]]
    val lst = scala.collection.mutable.ListBuffer[Response]()
    observable.subscribe(
      s => {
        lst.synchronized(lst.+=(s))
        Continue
      },
      e =>
        p.tryFailure(e),
      () =>
        p.trySuccess(lst.toList)
    )

    p.future
  }

//  case class Request(accountId: String, taskId: String = presentation.getTaskId) extends IdentityTask
//  case class Response(result: Option[String], taskId: String) extends IdentityTask
  case class Request(accountId: String)
  case class Response(result: Option[String])
}

/**
OUTPUT:
ForkJoinPool-1-worker-9:1471133677987 - List(Response(Some(foo),ForkJoinPool-1-worker-9197464411151476), Response(Some(boo),ForkJoinPool-1-worker-9197464411151476))
*/