name := "MiniMOGame-Server"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.6.0-M3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe" % "config" % "1.3.3",
  "org.postgresql" % "postgresql" % "42.1.4",
  "io.getquill" %% "quill-jdbc" % "2.4.2",
  "junit" % "junit" % "4.12" % Test,

  "com.scalachan" %% "rxsocket" % "0.13.0",
)
