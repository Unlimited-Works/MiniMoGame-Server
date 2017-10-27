package minimo.rxsocket.session

import java.net.{InetSocketAddress, SocketAddress}
import java.nio.ByteBuffer
import java.nio.channels.{CompletionHandler, AsynchronousSocketChannel}

import minimo.rxsocket._
import minimo.rxsocket.dispatch.{TaskManager, TaskKey}
import minimo.rxsocket.session.implicitpkg._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

/**
  *
  */
class ClientEntrance(remoteHost: String, remotePort: Int) {
  val channel: AsynchronousSocketChannel = AsynchronousSocketChannel.open
  val serverAddr: SocketAddress = new InetSocketAddress(remoteHost, remotePort)

  private val heartBeatManager = new TaskManager()
  def connect = {
    val p = Promise[ConnectedSocket]
    channel.connect(serverAddr, channel, new CompletionHandler[Void, AsynchronousSocketChannel]{
      override def completed(result: Void, attachment: AsynchronousSocketChannel): Unit = {
        rxsocketLogger.log(s"linked to server success", 1)
        val connectedSocket = new ConnectedSocket(attachment, heartBeatManager,
          AddressPair(channel.getLocalAddress.asInstanceOf[InetSocketAddress], channel.getRemoteAddress.asInstanceOf[InetSocketAddress])
        )
        heartBeatManager.addTask(new HeartBeatSendTask(
          TaskKey(connectedSocket.addressPair.remote + ".SendHeartBeat", System.currentTimeMillis() + Configration.SEND_HEART_BEAT_BREAKTIME * 1000L),
            Some(-1, Configration.SEND_HEART_BEAT_BREAKTIME * 1000L),
            connectedSocket
          )
        )
        heartBeatManager.addTask(new HeartBeatCheckTask(
            TaskKey(connectedSocket.addressPair.remote + ".CheckHeartBeat", System.currentTimeMillis() + Configration.CHECK_HEART_BEAT_BREAKTIME * 1000L),
            Some(-1, Configration.CHECK_HEART_BEAT_BREAKTIME * 1000L),
            connectedSocket
          )
        )

        p.trySuccess(connectedSocket)
      }

      override def failed(exc: Throwable, attachment: AsynchronousSocketChannel): Unit = {
        rxsocketLogger.log(s"linked to server error - $exc", 1)
        p.tryFailure(exc)
      }
    })

    /**
      * to test
      * 目前没有合适的方式来测试超时异常是否能起作用
      * 之前想到测试的方式是将服务端的端口绑定好ip地址,但不执行监听行为,但是就算这样,客户端依然认为是连接成功.
      */
    p.future.withTimeout(Configration.CONNECT_TIME_LIMIT * 1000)
  }
}
