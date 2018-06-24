package minimo.network

import java.nio.ByteBuffer

import minimo.rxsocket.session.PassiveParser

/**
  * sync protocol such as position, attack which should be quickly to handled and
  * avoid json parsing which is waste time.
  * besides, message is specify to deal with. recommend use classic json parser for normal works.
  *
  */
class SyncParser(handlers: Map[Short, SyncProtoHandler]) extends PassiveParser[SyncProto](2, 'init) {
  var currentProto = -1 //-1: SyncParser not parsing protocol
  var currentHandler: SyncProtoHandler = _

  override protected def passiveReceive(symbol: Symbol, length: Int, data: Array[Byte]): (Symbol, Int, Option[SyncProto]) = {
    currentProto match {
      case -1 => //the callback is beginning of the proto
        // setting proto
        assert(length == 2)
        val protoId = ByteBuffer.wrap(data).getShort()

        handlers.get(protoId) match {
          case None =>
            throw new Exception(s"not match any protoId - $protoId")

          case Some(handler) =>
            currentProto = protoId
            currentHandler = handler
            (handler.initSymbol, handler.initLength, None)
        }
      case _ =>
        val rst = currentHandler.parse(symbol, length, data)
        val (_, _, proto) = rst

        proto match {
          case Some(_) =>
            currentProto = -1
            currentHandler = null
            (initSymbol, initLength, proto)
          case None =>
            rst
        }

    }

  }

}

abstract class SyncProtoHandler( val initSymbol: Symbol,
                                 val initLength: Int
                               ) {

  def parse(symbol: Symbol, length: Int, proto: Array[Byte]): (Symbol, Int, Option[SyncProto])

}


class PositionSyncHandler() extends SyncProtoHandler('positionInit,12) {

  override def parse(symbol: Symbol, length: Int, proto: Array[Byte]): (Symbol, Int, Option[SyncProto]) = {
    (symbol, length) match {
      case ('positionInit, `initLength`) =>
        val data = ByteBuffer.wrap(proto)
        ('x, 0, Some(PositionProto(
          data.getFloat(),
          data.getFloat(),
          data.getFloat()
        )))
    }
  }

}

abstract class SyncProto {
  val unit: SyncProto
}

case class PositionProto(x: Float, y: Float, z: Float) extends SyncProto {
  override val unit: SyncProto = PositionProto.unit
}
object PositionProto {
  val unit: PositionProto = PositionProto(0, 0, 0)
}
