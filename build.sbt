name := "MiniMOGame-Server"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.6.0-M2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe" % "config" % "1.3.1",
  "org.postgresql" % "postgresql" % "42.1.4",
  "io.getquill" %% "quill-jdbc" % "2.3.1",
  "junit" % "junit" % "4.12" % Test,

  "com.scalachan" %% "rxsocket" % "0.11.0",
)
