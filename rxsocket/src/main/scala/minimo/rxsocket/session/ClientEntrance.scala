package minimo.rxsocket.session

import java.net.{InetSocketAddress, SocketAddress}
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import org.slf4j.LoggerFactory
import minimo.rxsocket.session.implicitpkg._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

/**
  *
  */
class ClientEntrance[Proto](remoteHost: String, remotePort: Int, genParser: () => ProtoParser[Proto]) {
  private val logger = LoggerFactory.getLogger(getClass)

  val channel: AsynchronousSocketChannel = AsynchronousSocketChannel.open
  val serverAddr: SocketAddress = new InetSocketAddress(remoteHost, remotePort)

  def connect: Future[ConnectedSocket[Proto]] = {
    val p = Promise[ConnectedSocket[Proto]]

    channel.connect(serverAddr, channel, new CompletionHandler[Void, AsynchronousSocketChannel]{
      override def completed(result: Void, attachment: AsynchronousSocketChannel): Unit = {
        logger.debug(s"linked to server success")
        val connectedSocket = new ConnectedSocket(attachment,
//          heartBeatManager,
          AddressPair(channel.getLocalAddress.asInstanceOf[InetSocketAddress], channel.getRemoteAddress.asInstanceOf[InetSocketAddress]),
          false,
          genParser()
        )

//        logger.info("add heart beat task")
//        heartBeatManager.addTask(new HeartBeatSendTask(
//          TaskKey(connectedSocket.addressPair.remote + ".SendHeartBeat", System.currentTimeMillis() + Configration.SEND_HEART_BEAT_BREAKTIME * 1000L),
//            Some(-1, Configration.SEND_HEART_BEAT_BREAKTIME * 1000L),
//            connectedSocket
//          )
//        )

        p.trySuccess(connectedSocket)
      }

      override def failed(exc: Throwable, attachment: AsynchronousSocketChannel): Unit = {
        logger.debug(s"linked to server error - $exc")
        p.tryFailure(exc)
      }
    })

    p.future
     .withTimeout(Configration.CONNECT_TIME_LIMIT * 1000)
  }
}
