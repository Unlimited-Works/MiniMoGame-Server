package minimo.rxsocket.dispatch

import minimo.util.ObjectId

case class TaskKey(id: String, systemTime: Long)

trait Task {
  val taskId: TaskKey //account and custom name
  def execute(): Unit
  def nextTask: Option[Task] //able to execute next time, completed as None
  override def toString = {
    super.toString + s"-$taskId"
  }
}

object Task {
  /**
    * combine with thread and current time to identity this task
    */
  def getId: String = {
    new ObjectId().toString
  }
}