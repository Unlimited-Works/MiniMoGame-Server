name := "MiniMOGame-Server"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "io.reactivex" %% "rxscala" % "0.26.5",
  "org.json4s" %% "json4s-native" % "3.6.0-M1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe" % "config" % "1.3.1"
)
