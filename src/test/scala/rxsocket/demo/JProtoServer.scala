package rxsocket.demo

import org.json4s.JsonAST.JString
import org.json4s.native.JsonMethods._
import minimo.rxsocket.presentation.json.JProtocol
import minimo.rxsocket.session.{CommPassiveParser, ServerEntrance}
import monix.execution.Ack.Continue
import monix.execution.Scheduler.Implicits.global

/**
  * Json presentation Example
  */
object JProtoServer extends App{
  val socket = new ServerEntrance("127.0.0.1", 10012, () => new CommPassiveParser()).listen

  val jprotoSocket = socket.map(connection => new JProtocol(connection, connection.startReading))

  case class Response(result: Option[String])

  jprotoSocket.subscribe { s =>
    s.jRead.subscribe{ j =>
      println(s"GET_INFO - ${compact(render(j))}")
      val JString(tskId) = j \ "taskId" //assume has taskId for simplify
      //send multiple msg with same taskId as a stream
      s.send(Response(Some("foo")), tskId)
      s.send(Response(Some("boo")), tskId)
      s.send(Response(None), tskId)
      Continue
    }
    Continue
  }

  Thread.currentThread().join()
}

/**
OUTPUT:
Thread-11:1471133677663 - connect - success
GET_INFO - {"accountId":"admin","taskId":"ForkJoinPool-1-worker-9197464411151476"}
*/