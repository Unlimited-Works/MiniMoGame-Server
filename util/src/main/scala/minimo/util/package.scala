package minimo

import java.math.BigInteger
import java.security.MessageDigest

/**
  *
  */
package object util {
  /**
    * get a md5 result with src
    * @param src
    * @return md5 string
    */
  def md5(src: String): String = { // 生成一个MD5加密计算摘要
    val md = MessageDigest.getInstance("MD5")
    // 计算md5函数
    md.update(src.getBytes)
    // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
    // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
    new BigInteger(1, md.digest).toString(16)
  }
}
