package minimo.dao

import org.junit.Test

/**
  *
  */
class TestDao {

  @Test
  def createUser: Unit = {
    val rst = UserDao.saveUser("user3", "pwd13")
    println("result objectId: " + rst)
  }

  @Test
  def findUser: Unit = {
    val rst = UserDao.checkUserPwd("user2", "pwd2")
    println("result User: " + rst)
  }

}
