name := "MiniMOGame-Server"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "io.reactivex" %% "rxscala" % "0.26.5",
  "org.json4s" %% "json4s-native" % "3.6.0-M1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe" % "config" % "1.3.1",
  "org.postgresql" % "postgresql" % "42.1.4",
  "io.getquill" %% "quill-jdbc" % "2.2.0",
  "junit" % "junit" % "4.12" % Test,
)
