package minimo.dao



/**
  *
  */
object UserDao {
  import ctx._

  case class Users(oid: ObjectId, username: String, pwd: String)

  def saveUser(userName: String, password: String): ObjectId = {
    val oid = new ObjectId()

    val q = quote {
      query[Users].insert(lift(Users(oid, userName, password)))
    }
    run(q)
    oid
  }

  def checkUserPwd(username: String, password: String): Option[Users] = {
    val q = quote {
      query[Users].filter(user => user.username == lift(username) && user.pwd == lift(password)).take(1)
    }
    run(q).headOption
  }
}
