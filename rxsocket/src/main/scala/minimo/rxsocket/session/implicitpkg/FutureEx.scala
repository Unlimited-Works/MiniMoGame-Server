package minimo.rxsocket.session.implicitpkg

import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

case object FutureTimeoutException extends RuntimeException
case object FutureTimeoutNotOccur extends RuntimeException

class FutureEx[T](f: Future[T]){

  // use a waitExecutor to handle sleep
  def withTimeout(ms: Long = 2000): Future[T] = {

    Future.firstCompletedOf(List(f, {
      val p = Promise[T]
      import scala.concurrent.duration._
      import monix.execution.Scheduler.{global => scheduler}

      val timeoutScheduler = scheduler.scheduleOnce(ms.millis) {
        p.tryFailure(FutureTimeoutNotOccur)
      }

      f.onComplete(_ => {
        // 源Futrue完成之后，如果定时器Future没有完成，则取消定时器的调度工作
        if(!p.isCompleted) {
          timeoutScheduler.cancel()
        }
      })
      p.future
    }))

  }

  def withTimeout(duration: Duration)
                  : Future[T] = withTimeout(duration.toMillis)
}
