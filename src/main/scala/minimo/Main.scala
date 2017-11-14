package minimo

import minimo.viewfirst.login.RouterLogin

/**
  *
  */
object Main extends App {
  //init various resource
  Config
  new Network(new RouterLogin :: Nil)

  Thread.currentThread().join()
}
