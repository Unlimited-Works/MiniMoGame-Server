package minimo.route

import minimo.network.jsonsocket.{EndPoint, JRouter}
import minimo.network.jsession.MinimoSession
import minimo.network.syncsocket.{InitSessionProto, SyncProto, SyncRouter}
import org.json4s.JsonAST

import scala.concurrent.Future

class SessionMockRouter extends JRouter with SyncRouter {
  override val jsonPath: String = "session_mock_path"

  override def jsonRoute(protoId: String, load: JsonAST.JValue)(implicit session: MinimoSession): EndPoint = {
    protoId match {
//      case "gen_session" =>
//        val uuid = UUID.randomUUID().toString
//        session.updateData(s => {
//          s.map(a => a)
//        })
//        RawEndPoint(JString(uuid))
      case "goto_scene" =>
        ???
    }
  }

  override def syncFn(data: SyncProto, session: MinimoSession): Future[Unit] = {
    data match {
      case InitSessionProto(sessionID) =>
        MinimoSession.findById(sessionID)

        Future.successful(())
//      case "goto_scene" =>
//        ???
    }
  }
}
