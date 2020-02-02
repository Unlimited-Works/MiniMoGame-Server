package minimo

package object module {
  case class Position(x: Float, y: Float, z: Float)
  case class Player(id: String, name: String,
                    //                    jsonSocket: ConnectedSocket[CompletedProto], syncSocket: ConnectedSocket[SyncProto],
                    var property: Property, var position: Position)
  case class Property(var speed: Float,//角色移动速度
                      var power: Int,//炸弹长度
                      var count: Int//炸弹个数
                     )

}
