package minimo.service

import api.UserService
import minimo.dao.{ObjectId, UserDao}
import minimo.util

/**
  * implement UserService api
  */
class UserServiceImp extends UserService {

  override def loginVerify(userName: String, password: String): Option[ObjectId] = {
    UserDao.checkUserPwd(userName, password)

  }

  override def registerAccount(userName: String, password: String): Either[String, ObjectId] = synchronized {
    UserDao.checkUserExist(userName) match {
      case None =>
        Right(UserDao.saveUser(userName, util.md5(password)))
      case Some(_) =>
        Left("user_already_exist")
    }

  }

}
