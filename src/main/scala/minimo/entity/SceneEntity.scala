package minimo.entity

import minimo.util.ObjectId

import scala.collection.mutable

/**
  * a SceneEntity is a playing game scene which contains:
  * 1. map data(todo)
  * 2. plyaer data (position action)
  */
case class SceneEntity(sceneId: ObjectId,
                        players: mutable.Map[ObjectId, PlayerEntity],
                       ) {


}

object SceneEntity {
  def apply(palyers: List[PlayerEntity]): Unit = {

  }
}