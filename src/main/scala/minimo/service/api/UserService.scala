package minimo.service.api

import minimo.util.ObjectId

/**
  * user relative api
  */
trait UserService {

  /**
    * verify userName and userPassword match on users table in db.
    * should do md5 to transform password
    * @param userName
    * @param password get a password from client input
    * @return Some(oid) if success, otherwise None
    */
  def loginVerify(userName: String, password: String): Option[ObjectId]

  /**
    * create new user
    * @param userName
    * @param password should be transform with md5 before save to db
    * @return Right[ObjectId] if success, Left[String] if failure
    */
  def registerAccount(userName: String, password: String): Either[String, ObjectId]
}
