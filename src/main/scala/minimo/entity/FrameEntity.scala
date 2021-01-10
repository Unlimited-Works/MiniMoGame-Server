package minimo.entity

import java.util.concurrent.ConcurrentHashMap

import minimo.entity.FrameEntity.{FrameData, FrameValue}
import minimo.util.ObjectId
import monix.reactive.Observable
import monix.reactive.subjects.ReplaySubject

import scala.collection.mutable
import scala.collection.mutable.{HashMap => MHashMap}

/**
  * record game scene frame data
  */
case class FrameEntity(idFrame: ObjectId,
                        playerAndCmds: MHashMap[ObjectId, List[String]],
                        frames: MHashMap[Long, MHashMap[ObjectId, List[String]]],
                        systemPlayerId: ObjectId, // 代表系统本身
                        var currFrameCount: Long,
                        frameStream: Observable[FrameData],
                        frameCount: Int,

                ) {
  private var gameLoop: Thread = _
  private var isRunning = true
  private var frameSubject: ReplaySubject[FrameData] = _

  // 覆盖操作
  // todo 按照输入模块进行覆盖。比如攻击操作，移动操作的指令在同一帧情况下，可以同时存在
  def putCurFrame(playerId: ObjectId, cmds: List[String]): Unit = this.synchronized {
    val theCurFrameCount = currFrameCount
    val frameOpt = frames.get(theCurFrameCount)
    frameOpt match {
      case Some(frame) =>
        frame.put(playerId, cmds)
      case None =>
        val frame = new MHashMap[ObjectId, List[String]]()
        frame.put(playerId, cmds)
        frames.put(theCurFrameCount, frame)
    }
  }

  private def putNewFrame(playerId: ObjectId, cmds: List[String]): Unit = this.synchronized {
    val nextFrameCount = currFrameCount + 1
    currFrameCount += 1

    val frameOpt = frames.get(nextFrameCount)
    frameOpt match {
      case Some(frame) =>
        frame.put(playerId, cmds)
      case None =>
        val frame = new MHashMap[ObjectId, List[String]]()
        frame.put(playerId, cmds)
        frames.put(nextFrameCount, frame)
    }
  }

  protected def startGameLoop(subject: ReplaySubject[FrameData]):
//            Observable[(Long, Map[ObjectId, List[String]])] = {
            Observable[FrameData] = {
    frameSubject = subject
    // todo: 1. await 3s and do frame command with server side FPS setting
    //       2. use thread pool and timer wheel scheduler
    val theGameLoop = new Thread(() => {
      while (isRunning) {
        val curTime = System.currentTimeMillis()

        // do logic: 转发这一帧收集的消息
        val curFC = this.currFrameCount
        val frameData = frames.get(curFC).get
        val frameValues = frameData.toList.map{case (k, v) =>
          FrameValue(k, v)
        }

        subject.onNext(FrameData(curFC, frameValues))


        // 创建一个新的帧信息
        putNewFrame(systemPlayerId, List("init"))

        Thread.sleep(1000 / frameCount - ( System.currentTimeMillis() - curTime))
      }
    })
    theGameLoop.start()
    gameLoop = theGameLoop

    subject
  }

  def closeGame(): Unit = {
    isRunning = false
    frameSubject.onComplete()
  }

}

/**
  * create FrameEntity, maintain frame data.
  */
object FrameEntity {
  private val frameEntities = new ConcurrentHashMap[ObjectId, FrameEntity]()
  private val roomIdAndFrameEntities = new ConcurrentHashMap[ObjectId, FrameEntity]()

  case class FrameData(frameCount: Long, frameValues: List[FrameValue])
  case class FrameValue(userId: ObjectId, cmds: List[String])
  // create a FrameEntity or get from exist
  def apply(roomId: ObjectId, netFrame: Int): FrameEntity = {
    var newCreated: Boolean = false
    var subject: ReplaySubject[FrameData] = null

    val fe = roomIdAndFrameEntities.compute(roomId, (_, v) => {
      if(v == null) {
        val idFrame = new ObjectId()
        val playerAndCmds = MHashMap[ObjectId, List[String]]()
        val frames = MHashMap[Long, MHashMap[ObjectId, List[String]]]()
        val systemPlayerId = new ObjectId()
        val firstFrameCount = 0L
        frames.put(firstFrameCount, {
          val firstFrameData = new MHashMap[ObjectId, List[String]]()
          firstFrameData.put(systemPlayerId, List("init"))
          firstFrameData
        })

        subject = ReplaySubject[FrameData]()

        val entity = new FrameEntity(idFrame, playerAndCmds,frames,
          systemPlayerId, firstFrameCount, subject, netFrame)

        frameEntities.put(idFrame, entity)
        newCreated = true
        entity
      } else {
        v
      }
    })

    // create a game loop
    if(newCreated) {
      fe.startGameLoop(subject)
    }

    fe

  }

  def getFrameEntity(idFrame: ObjectId): Option[FrameEntity] = {
    Option(frameEntities.get(idFrame))
  }

  def getByRoomId(idRoom: ObjectId): Option[FrameEntity] = {
    Option(roomIdAndFrameEntities.get(idRoom))
  }

}