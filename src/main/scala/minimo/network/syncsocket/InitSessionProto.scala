package minimo.network.syncsocket

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods


class InitSessionSyncHandler() extends SyncProtoHandler(Symbol("initSession"),2) {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  override def parse(symbol: Symbol, length: Int, proto: Array[Byte]): (Symbol, Int, Option[SyncProto]) = {
    (symbol, length) match {
      case (Symbol("initSession"), `initLength`) =>
        (Symbol("sessionData"), ByteBuffer.wrap(proto).getShort, None)
      case (Symbol("sessionData"), _) =>
        val data = ByteBuffer.wrap(proto)
        val jsonString = new String(data.array(), StandardCharsets.UTF_8)

        val initData = JsonMethods.parse(jsonString).extract[InitSessionProto]

        (Symbol("initSession"), 2, Some(initData))
    }
  }

}

// session init
// todo: use JWT insure addrId is reliable
case class InitSessionProto(sessionID: String) extends SyncProto {
  override val unit: SyncProto = InitSessionProto.unit

  override val encode: Array[Byte] = {
    sessionID.getBytes(StandardCharsets.UTF_8)
  }
}

object InitSessionProto {
  val unit: InitSessionProto = InitSessionProto("")
}
