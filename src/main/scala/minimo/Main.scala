package minimo

import minimo.network.{Network, PositionProto, SyncProto}
import org.slf4j.LoggerFactory
import route.{LoginRouter, RoomRouter, SceneRouter}

/**
  *
  */
object Main extends App {
  private val logger = LoggerFactory.getLogger(this.getClass)

  //init config
  MinimoConfig

  //forbid heart beat for simple
//  lorance.rxsocket.session.Configration.CHECK_HEART_BEAT_BREAKTIME = Int.MaxValue
//  lorance.rxsocket.session.Configration.SEND_HEART_BEAT_BREAKTIME = Int.MaxValue
  val sceneRouter = new SceneRouter()

  val jroutes = Map(
    "login" -> new LoginRouter,
    "room" -> new RoomRouter,
    "scene" -> sceneRouter,

  )

  val syncRouters: Map[SyncProto, SceneRouter] = Map(
    PositionProto.unit -> sceneRouter,

  )

  //start network server
  logger.info(s"starting network at: ${MinimoConfig.network}")
  new Network(
    MinimoConfig.network.host,
    MinimoConfig.network.port,
    MinimoConfig.network.syncPort,
    jroutes,
    syncRouters
  )


  Thread.currentThread().join()
}
