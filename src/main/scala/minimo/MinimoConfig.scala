package minimo

import java.io.File

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import org.slf4j.LoggerFactory

/**
  *
  */
object MinimoConfig {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val fileConf = ConfigFactory.parseFile(new File("./application.conf"))
  private val online = ConfigFactory.parseResourcesAnySyntax("online")
  private val local = ConfigFactory.parseResourcesAnySyntax("local")
  private val develop = ConfigFactory.parseResourcesAnySyntax("application")
  private val default = ConfigFactory.load() //default environment

  //global config
  val myConfig: Config = fileConf.withFallback(online).withFallback(local).withFallback(develop)
  val combinedConfig: Config = myConfig.withFallback(default)

  //library or custom config
  val quill: Config = combinedConfig.getConfig("ctx")
  val network: Network = {
    val nconfig = combinedConfig.getConfig("minimo.network")
    Network(nconfig.getString("host"), nconfig.getInt("port"))
  }

  def printConf(config: Config, info: String = ""): Unit = logger.info(info + "\n" + config.root().render(ConfigRenderOptions.concise().setFormatted(true).setJson(true)))


  MinimoConfig.printConf(MinimoConfig.myConfig, "my_config")
  MinimoConfig.printConf(MinimoConfig.combinedConfig, "combined_config")

  case class Network(host: String, port: Int)
}
