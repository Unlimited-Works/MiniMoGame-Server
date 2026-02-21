package minimo.dao

import minimo.util.ObjectId
import java.util.concurrent.ConcurrentHashMap

object UserDaoMock extends UserDaoLike {
  private case class UserRecord(oid: ObjectId, pwd: String)
  private val users = new ConcurrentHashMap[String, UserRecord]()

  def saveUser(userName: String, password: String): ObjectId = {
    val oid = new ObjectId()
    users.put(userName, UserRecord(oid, password))
    oid
  }

  def checkUserPwd(username: String, password: String): Option[ObjectId] =
    Option(users.get(username)).filter(_.pwd == password).map(_.oid)

  def checkUserExist(username: String): Option[Any] =
    Option(users.get(username))
}
