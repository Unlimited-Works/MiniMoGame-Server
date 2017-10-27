package minimo.rxsocket

import scala.collection.mutable
import com.typesafe.scalalogging.Logger

trait Log {
  private val logger: Logger = Logger("rxsocket")
  // a threshold of level filter small.
  var logLevel = 0

  var logAim = mutable.ListBuffer[String]()

  /**
    * @param level higher represent more detail message
    */
  def log(msg: Any, level: Int = 0, aim: Option[String] = None): Unit = {
    if (level <= this.logLevel || (aim.nonEmpty && logAim.contains(aim.get))) {
      logger.info(s"${Thread.currentThread.getName}:${System.currentTimeMillis()} - ${aim.map(a => s"[$a] - ").getOrElse("")}$msg")
      customLog(msg, level, aim)
    }
  }

  protected def customLog(msg: Any, level: Int = 0, aim: Option[String]): Unit

}

case class ConsoleLog() extends Log {
  override protected def customLog(msg: Any, level: Int = 0, aim: Option[String]) = Unit
}