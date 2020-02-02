package minimo.rxsocket

import java.util.concurrent.ConcurrentHashMap

package object actor {
  // all actor message put to the mail box
  // key: actor instance
  // value: messages
  private[actor] val mailBox = new ConcurrentHashMap[Object, List[Object]]()
}
