package minimo

import java.util.concurrent.TimeUnit

import minimo.dao.InitDB
import minimo.network.Network
import minimo.network.syncsocket.{PositionProto, SyncProto}
import monix.execution.Scheduler
import org.slf4j.LoggerFactory
import route.{LobbyRouter, LoginRouter, SceneRouter, TurnSyncInitRouter, TurnSyncRouter}

/**
  *
  */
object Main extends App {
  private val logger = LoggerFactory.getLogger(this.getClass)

  //init config
  MinimoConfig

  import monix.execution.Scheduler.{global => scheduler}
  val c = scheduler.scheduleWithFixedDelay(
    0, 15, TimeUnit.SECONDS,
    new Runnable {
      def run(): Unit = {
        logger.debug("init DB: " + InitDB.select2())
      }
    })

  //forbid heart beat for simple
//  minimo.rxsocket.session.Configration.CHECK_HEART_BEAT_BREAKTIME = Int.MaxValue
//  minimo.rxsocket.session.Configration.SEND_HEART_BEAT_BREAKTIME = Int.MaxValue
  val sceneRouter = new SceneRouter()

  val jroutes = List(
    new LoginRouter,
//    new RoomRouter,
    new LobbyRouter,
    sceneRouter,
    new TurnSyncInitRouter,
    new TurnSyncRouter
  )

  val syncRouters: Map[SyncProto, SceneRouter] = Map(
    PositionProto.unit -> sceneRouter,

  )

  //start network server
  logger.info(s"starting network at: ${MinimoConfig.network}")
  val network = new Network(
    MinimoConfig.network.host,
    MinimoConfig.network.port,
    MinimoConfig.network.syncPort,
    jroutes,
    syncRouters
  )


  Thread.currentThread().join()

}
