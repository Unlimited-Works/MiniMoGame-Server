package minimo.network

import org.json4s.JsonAST.JValue
import rx.lang.scala.Observable

import scala.concurrent.Future
import scala.util.Try

/**
  * EndPoint represent request and response
  */
trait EndPoint
case class RawEndPoint(value: Try[JValue]) extends EndPoint
case class FurEndPoint(value: Future[JValue]) extends EndPoint
case class StreamEndPoint(value: Observable[JValue]) extends EndPoint
