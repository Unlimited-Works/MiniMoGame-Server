package minimo.dao

import minimo.util.ObjectId

/**
  *
  */
object UserDao {
  import ctx._

  case class Users(oid: ObjectId, userName: String, pwd: String)

  def saveUser(userName: String, password: String): ObjectId = {
    val oid = new ObjectId()

    val q = quote {
      query[Users].insert(lift(Users(oid, userName, password)))
    }
    run(q)
    oid
  }

  def checkUserPwd(username: String, password: String): Option[ObjectId] = {
    val q = quote {
      query[Users].filter(user => user.userName == lift(username) && user.pwd == lift(password)).map(_.oid).take(1)
    }
    run(q).headOption
  }

  def checkUserExist(username: String): Option[Users] = {
    val q = quote {
      query[Users].filter(user => user.userName == lift(username)).take(1)
    }
    run(q).headOption

  }
}
