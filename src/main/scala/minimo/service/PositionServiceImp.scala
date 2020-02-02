package minimo.service

import java.util.concurrent.ConcurrentHashMap

import minimo.module.Position
import minimo.service.api.PositionService


object PositionServiceImp extends PositionService {
  private val posMap = new ConcurrentHashMap[String, Position]()

  override def setPos(objId: String, pos: Position): Unit = {
    posMap.put(objId, pos)
  }

  override def getPos(objId: String): Option[Position] = {
    Option(posMap.get(objId))
  }
}
