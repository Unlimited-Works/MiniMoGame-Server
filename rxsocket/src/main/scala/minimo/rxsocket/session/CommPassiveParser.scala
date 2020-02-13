package minimo.rxsocket.session

import java.nio.ByteBuffer

import minimo.rxsocket.session
import minimo.rxsocket.session.implicitpkg._

class CommPassiveParser(private var tmpProto: PaddingProto) extends PassiveParser[CompletedProto](1, 'init) {

  def this() = {
    this(PaddingProto(None, None, session.EmptyByteBuffer))
  }

  override protected def passiveReceive(symbol: Symbol, length: Int, data: Array[Byte]): (Symbol, Int, Option[CompletedProto]) = {
    (symbol, length) match {
      case (Symbol("init"), 1) => //proto type
        tmpProto = PaddingProto(Some(data(0)), None, session.EmptyByteBuffer)
        (Symbol("length"), 4, None)
      case (Symbol("length"), 4) => //load length
        val curLoadLength = data.toInt
        tmpProto = PaddingProto(tmpProto.uuidOpt, Some(CompletedLength(curLoadLength)), session.EmptyByteBuffer)
        (Symbol("load"), curLoadLength, None)
      case (Symbol("load"), _) => //load data
        (Symbol("init"), 1, Some(CompletedProto(
          tmpProto.uuidOpt.get,
          tmpProto.lengthOpt.get.asInstanceOf[CompletedLength].length,
          ByteBuffer.wrap(data)
        )))
      case _ =>
        throw ProtoParseError(s"should NOT arrive - symbol=$symbol, length=$length")

    }
  }
}
