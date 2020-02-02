//package minimo.scene
//
//import java.util.concurrent.ConcurrentHashMap
//
//import minimo.rxsocket.session.{CompletedProto, ConnectedSocket}
//import ObjectId
//
///**
//  * 基本场景包含相关的这几个上下文数据：
//  *   1. 房间名
//  *   2. 房间socket的链接
//  *   3. 地图的实例化对象
//  *   4.
//  */
//trait BaseScene {
//  protected val objLock = new Object
//  protected val roomId: ObjectId = new ObjectId()
//  protected val sockets: ConcurrentHashMap[String, ConnectedSocket[CompletedProto]]
//  protected val objects: ConcurrentHashMap[ObjectId, GameObject]
//  protected val objectPos: ConcurrentHashMap[ObjectId, Position]
//
//
//  def updatePos(objId: ObjectId, newPos: Position): Unit = {
//    objectPos.replace(objId, newPos)
//  }
//
//  def getAllPos: Unit = {
////    objects.
//  }
//}
