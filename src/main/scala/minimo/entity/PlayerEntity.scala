package minimo.entity

import minimo.entity.PlayerEntity.{Direction, Position, Role}
import minimo.route.LoginRouter.UserInfo
import minimo.util.ObjectId

/**
  * manage a player info in game scene
  */
case class PlayerEntity(playerId: ObjectId,
                        userInfo: UserInfo,
                        role: Role.Value,
                        
                        var position: Position,
                        var direction: Direction.Value) {

  def syncPositionAndDirection(position: Position, direction: Direction.Value): Unit = {
    this.position = position
    this.direction = direction
  }


}

object PlayerEntity {
  case class Position(x: Int, y: Int)
  object Direction extends Enumeration {
    val UP = Value(0, "UP")
    val DOWN = Value(0, "DOWN")
    val LEFT = Value(0, "LEFT")
    val RIGHT = Value(0, "RIGHT")
  }
  object Role extends Enumeration {
    val LILI = Value(0, "莉莉")
    val PIRATE = Value(1, "海贼王")
  }
  case class PlayerInfo(role: Role.Value)

  def apply(): Unit = {

  }
}