//package minimo.scene
//
//import minimo.dao.ObjectId
//
//trait GameObject {
//  val name: String
//  val id: ObjectId
//
//}
//
//case class Player(override val id: ObjectId,
//                  override val name: String,
//                 ) extends GameObject
//
//case class Block(override val id: ObjectId,
//                 override val name: String,
//                ) extends GameObject
//
//case class Tree(override val id: ObjectId,
//                override val name: String,
//               ) extends GameObject
//
//
//case class Position(var x: Double, y: Double, z: Double)