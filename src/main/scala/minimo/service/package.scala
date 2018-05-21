package minimo

import lorance.rxsocket.session.{CompletedProto, ConnectedSocket}
import minimo.network.SyncProto
import minimo.service.api.UserService

/**
  * instance of all service
  */
package object service {
  implicit val userService: UserService = new UserServiceImp

  case class ServiceContext(
                             ayncSocket: ConnectedSocket[SyncProto],
                             jsonSocket: ConnectedSocket[CompletedProto]
  )

}
