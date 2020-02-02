package minimo.network.syncsocket

import java.nio.ByteBuffer

import minimo.rxsocket.session.PassiveParser


/**
  * sync protocol such as position, attack which should be quickly to handled and
  * avoid json parsing which is waste time.
  * besides, message is specify to deal with. recommend use classic json parser for normal works.
  *
  * protocol:
  *  protoId: Short,
  *  position
  *
  */
class SyncParser(handlers: Map[Short, SyncProtoHandler]) extends PassiveParser[SyncProto](2, 'init) {
  var handlingProto = false
  //  var currentProto = -1 //-1: SyncParser not parsing protocol
  var currentHandler: SyncProtoHandler = _

  override protected def passiveReceive(symbol: Symbol, length: Int, data: Array[Byte]): (Symbol, Int, Option[SyncProto]) = {
    handlingProto match {
      case false => //the callback is beginning of the proto
        // setting proto
        assert(length == 2)
        val protoId = ByteBuffer.wrap(data).getShort()

        //每次都要查询？todo: 优化使用bitmap存储协议类型，因为协议类型不会太多
        handlers.get(protoId) match {
          case None =>
            throw new Exception(s"not match any protoId - $protoId")

          case Some(handler) =>
            handlingProto = true
            //            currentProto = protoId
            currentHandler = handler
            (handler.initSymbol, handler.initLength, None)
        }
      case _ =>
        val rst = currentHandler.parse(symbol, length, data)
        val (_, _, proto) = rst

        proto match {
          case Some(_) =>
            handlingProto = false
            //            currentProto = -1
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

abstract class SyncProto {
  val unit: SyncProto
  def encode: Array[Byte]
}
