import monix.eval.Task
import org.junit.Test

object MonixTaskTest extends App {

  def testFlatMap(): Unit = {

    val task = Task{
      println("task1")
      1
    }

    val task1 = Task{
      println("task2")
      10
    }

    val t3 = task.flatMap(x => {
      task1
    })


    Thread.sleep(1000 * 2)
    t3.runAsync

  }

  testFlatMap()
}
