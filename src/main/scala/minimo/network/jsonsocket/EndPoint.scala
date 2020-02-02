package minimo.network.jsonsocket

import monix.reactive.Observable
import org.json4s.JsonAST.JValue

import scala.concurrent.Future
import scala.util.Try
import org.json4s.Extraction._
import monix.execution.Scheduler.Implicits.global

/**
  * EndPoint represent request and response
  */
trait EndPoint
case object EmptyEndPoint extends EndPoint //no response
class RawEndPoint(val value: Try[JValue]) extends EndPoint

object RawEndPoint {
  def unapply(arg: RawEndPoint): Option[Try[JValue]] = Some(arg.value)
  def apply(value: Try[JValue]): RawEndPoint = new RawEndPoint(value)
  def apply(rawValue: JValue) = new RawEndPoint(Try(rawValue))
  def apply(caseClass: Any) = new RawEndPoint(Try(decompose(caseClass)))
}

case class FurEndPoint(value: Future[JValue]) extends EndPoint
object FurEndPoint {
  def apply2(caseClassFur: Future[Any]): FurEndPoint =
    new FurEndPoint(caseClassFur.map(decompose))
}
case class StreamEndPoint(value: Observable[JValue]) extends EndPoint
object StreamEndPoint {
  def fromAny(caseClassStream: Observable[Any]): StreamEndPoint =
    new StreamEndPoint(caseClassStream.map(decompose))
}
class ErrorEndPoint(val code: String, val desc: String, val ex: Throwable) extends EndPoint {
  override def toString: String = s"ErrorEndpoint(code: $code, desc: $desc, ex: $ex)"
}

// combine raw and stream for one request
case class RawAndStreamEndPoint(rawEndPoint: RawEndPoint, streamEndPoint: StreamEndPoint) extends EndPoint

object ErrorEndPoint {
  def apply(code: String, desc: String, ex: Throwable) = new ErrorEndPoint(code, desc, ex)
  def unapply(arg: ErrorEndPoint): Option[(String, String, Throwable)] = Some((arg.code, arg.desc, arg.ex))
}
case class SystemErrorEndPoint(override val ex: Throwable) extends ErrorEndPoint("SYSTEM_ERROR", ex.getMessage, ex)