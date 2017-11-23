package minimo

import minimo.service.api.UserService

/**
  * instance of all service
  */
package object service {
  implicit val userService: UserService = new UserServiceImp

}
