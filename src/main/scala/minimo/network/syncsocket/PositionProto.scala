package minimo.network.syncsocket

import java.nio.{ByteBuffer, ByteOrder}
import java.nio.charset.StandardCharsets


class PositionSyncHandler() extends SyncProtoHandler('positionInit,4 * 3 + 24) {

  override def parse(symbol: Symbol, length: Int, proto: Array[Byte]): (Symbol, Int, Option[SyncProto]) = {
    (symbol, length) match {
      case ('positionInit, `initLength`) =>
        val data = ByteBuffer.wrap(proto)
        ('positionInit, 4 * 3 + 24, Some(PositionProto(
          data.getFloat(),
          data.getFloat(),
          data.getFloat(),
          //object Id
          {
            val objId = new Array[Byte](24)
            data.get(objId, 0, 24)
            new String(objId, StandardCharsets.UTF_8)
          }
        )))
    }
  }

}

case class PositionProto(x: Float, y: Float, z: Float, objId: String) extends SyncProto {
  override val unit: SyncProto = PositionProto.unit

  override val encode = {
    ByteBuffer.allocate(4 * 3 + 24).order(ByteOrder.BIG_ENDIAN).
      putFloat(x).
      putFloat(y).
      putFloat(z).
      put(objId.getBytes(StandardCharsets.UTF_8)).
      array()
  }

}
object PositionProto {
  val unit: PositionProto = PositionProto(0, 0, 0, "")
}
