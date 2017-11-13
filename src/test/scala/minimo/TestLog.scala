package minimo

import org.slf4j.LoggerFactory

/**
  *
  */
object TestLog {
  val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    logger.debug("aaaa {}", {println("bbbbb"); "xxx"})
  }

}
