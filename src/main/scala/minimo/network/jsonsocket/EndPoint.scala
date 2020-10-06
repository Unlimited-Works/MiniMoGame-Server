package minimo.network.jsonsocket

import minimo.util.ObjectId
import monix.reactive.Observable
import org.json4s.JsonAST.JValue

import scala.concurrent.Future
import scala.util.Try
import org.json4s.Extraction._
import monix.execution.Scheduler.Implicits.global

trait SendMode
case object Non extends SendMode // 不发给任何人
case object Self extends SendMode // 仅发给自己
case class Zone(zoneId: ObjectId) extends SendMode // 发给同一区域的人
case class Zones(zoneIds: List[ObjectId], excludeSessions: List[ObjectId] = Nil, excludeSelf: Boolean = false) extends SendMode // 发给多个区域的人
case class MultipleSessions(userId: Set[String]) extends SendMode // 发给任意用户

/**
  * EndPoint represent request and response
  */
trait EndPoint {
  type T
  val value: T
  val sendMode: SendMode
}
case object EmptyEndPoint extends EndPoint {
  override type T = None.type
  override val value: None.type = None
  override val sendMode: SendMode = Non
}

class RawEndPoint(override val value: Try[JValue],
                  override val sendMode: SendMode) extends EndPoint {
  override type T = Try[JValue]
}

object RawEndPoint {
  def unapply(arg: RawEndPoint): Option[(Try[JValue], SendMode)] = Some(arg.value -> arg.sendMode)
  def apply(rawValue: JValue, sendMode: SendMode = Self): RawEndPoint = new RawEndPoint(Try(rawValue), sendMode)
  def fromTry(value: Try[JValue], sendMode: SendMode = Self): RawEndPoint = new RawEndPoint(value, sendMode)
  def fromCaseClass(caseClass: Any, sendMode: SendMode = Self): RawEndPoint = new RawEndPoint(Try(decompose(caseClass)), sendMode)
}

case class FurEndPoint(value: Future[JValue], sendMode: SendMode) extends EndPoint {
  override type T = Future[JValue]
}
object FurEndPoint {
  def apply2(caseClassFur: Future[Any], sendMode: SendMode = Self): FurEndPoint =
    new FurEndPoint(caseClassFur.map(decompose), sendMode)
}
case class StreamEndPoint(value: Observable[JValue], sendMode: SendMode) extends EndPoint {
  override type T = Observable[JValue]
}
object StreamEndPoint {
  def fromAny(caseClassStream: Observable[Any], sendMode: SendMode = Self): StreamEndPoint =
    new StreamEndPoint(caseClassStream.map(decompose), sendMode)
}

case class ErrorValue(code: String, desc: String, ex: Throwable)
class ErrorEndPoint(override val value: ErrorValue, override val sendMode: SendMode = Self) extends EndPoint {
  override type T = ErrorValue
  override def toString: String = s"ErrorEndpoint(code: ${value.code}, desc: ${value.desc}, ex: ${value.ex})"
}

object ErrorEndPoint {
  def unapply(arg: ErrorEndPoint): Option[(ErrorValue, SendMode)] = Option(arg.value -> arg.sendMode)
  def apply(value: ErrorValue, sendMode: SendMode = Self): ErrorEndPoint = new ErrorEndPoint(value, sendMode)

}

case class RawAndStreamValue(rawEndPoint: RawEndPoint, streamEndPoint: StreamEndPoint)
// combine raw and stream for one request
case class RawAndStreamEndPoint(value: RawAndStreamValue, sendMode: SendMode = Self) extends EndPoint {
  override type T = RawAndStreamValue
}


case class SystemErrorEndPoint(ex: Throwable, mode: SendMode = Self) extends ErrorEndPoint(ErrorValue("SYSTEM_ERROR", ex.getMessage, ex), mode)