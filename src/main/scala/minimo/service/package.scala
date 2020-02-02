package minimo

import minimo.service.api.UserService

/**
  * instance of all service.
  *
  * Service layer handle core concept of the game domain design which some similar as DDD.
  *
  * Service layer mainly best to description how the service works in the game. Each Service
  * provide full lifecycle of it's Domain Design.
  *
  * Service layer use core Data Structure as function parameters, which context should be give
  * by Router layer or save in client side in which design is good for performance and more
  * clear about context data is needed. Besides, Service layer should be take responsibility to ensure
  * every invoke is security to avoid mess data and break the domain lifecycle.
  */
package object service {
  implicit val userService: UserService = new UserServiceImp


}
