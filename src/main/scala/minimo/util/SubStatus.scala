package minimo.util

import monix.execution.{Ack, Cancelable}
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future

/**
  * handle Observable status.
  * Observable流不断的emit消息，当收到某些消息时，我们会期望改变消息的处理逻辑。
  * 消息的处理逻辑使用一个同步锁的curStatus变量进行表达，该变量为一个偏函数，能够处理该状态下的相关消息消费行为。
  * 并且在一定的条件下使用become函数改变curStatus的处理状态。
  *
  * 该trait融合了消息流和Actor的状态模式。
  */
trait SubStatus[T] {

  type Status = PartialFunction[T, Future[Ack]]

  val source: Observable[T]

  private var curStatus = init

  lazy val start: Cancelable = source.subscribe(item => {
    curStatus.apply(item)
  })

  protected def become(newStatus: Status): Unit = this.synchronized {

    curStatus = newStatus
  }

  protected def init: Status

}
