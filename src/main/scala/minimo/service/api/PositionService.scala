package minimo.service.api

import minimo.module.Position

/**
  * position service support cache position and read & write to the object
  * organized by roles which means one role has one position
  */
trait PositionService {

  def setPos(objId: String, pos: Position): Unit

  def getPos(objId: String): Option[Position]
}
