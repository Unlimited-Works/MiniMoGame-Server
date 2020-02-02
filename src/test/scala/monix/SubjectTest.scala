package monix

import java.util.concurrent.TimeUnit

import minimo.Main.logger
import minimo.dao.InitDB
import monix.execution.Ack.Continue
import monix.execution.Cancelable
import monix.reactive.{Observable, Observer}
import monix.reactive.observers.Subscriber
import org.junit.Test
import monix.execution.Scheduler.Implicits.global
import monix.reactive.subjects.{PublishSubject, PublishToOneSubject, ReplaySubject}

import scala.concurrent.duration.Duration

class SubjectTest {

  @Test
  def test(): Unit = {
    val value = Observable.interval(Duration.apply(1, TimeUnit.SECONDS)).publish
    val x = value.subscribe(a => {
      println(a)
      Continue
    })
    val y = value.subscribe(a => {
      println(a)
      Continue
    })
    Thread.sleep(4500)
    value.connect()
//    x.cancel()
//    y.cancel()
    Thread.sleep(3000)
    x.cancel()
    y.cancel()
    println("begin subscribe ")
    val z = value.subscribe(a => {
      println(a)
      Continue
    })

//    val w = value.subscribe(a => {
//      println(a)
//      Continue
//    })

    Thread.currentThread().join()
  }

  @Test
  def test2(): Unit = {
    val value = Observable.interval(Duration.apply(1, TimeUnit.SECONDS)).share

    val subject = ReplaySubject[Long]()
    value.subscribe(subject)
//    global.scheduleWithFixedDelay(
//      0, 1, TimeUnit.SECONDS,
//      new Runnable {
//        @volatile var a = 0
//        def run(): Unit = {
//          println(a)
//          a = a + 1
//          subject.onNext(a)
//        }
//      })

    Thread.sleep(5000)
    subject.subscribe(a => {
      println(s"observe ${a}")
      Continue
    })
//    subject.subscribe(a => {
//      println(s"observe2 ${a}")
//      Continue
//    })
    Thread.currentThread().join()

  }

}
