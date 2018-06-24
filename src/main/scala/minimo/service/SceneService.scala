package minimo.service

import java.util.concurrent.ConcurrentHashMap

import minimo.module.{Player, Position}

import scala.collection.mutable

/**
  * 每一个开始游戏后的对战地图都是一个SceneService, 对应的功能包括：
  * 1. 同步每个角色的位置信息
  * 2. 处理攻击行为
  * 3. 场景初始化
  * 4. 结束判定
  */
class SceneService(id: String,
                   name: String
                  ) {
  import SceneService._

  val playerMapLock = new Object

  type PlayerId = String
  val playerMap = mutable.Map[PlayerId, Player]()

  val playerPositions = mutable.Map[PlayerId, Position]()
  /**
    * return init data
    */
  def create() = {

  }

  def syncPosition(playerId: String, newPos: Position): Unit = playerMapLock.synchronized {
    playerMap.get(playerId).foreach(x => x.position = newPos)
  }

  def getPositions() = {
    playerMap.map{case (k, v) =>
      k -> v.position
    }
  }

  def setBomb(playerId: String, position: Position) = {

  }

  def destory() = {}

}

object SceneService {
  type SceneId = String
  private val map = new ConcurrentHashMap[SceneId, SceneService]()
  def apply(id: String,
            name: String
           ): SceneService = {
    val scene = new SceneService(id, name)
    map.put(id, scene)
    scene
  }

  def get(id: String): Option[SceneService] = {
    Option(map.get(id))
  }

  def remove(id: String): Option[SceneService] = {
    Option(map.remove(id))
  }

}