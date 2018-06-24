Global / name := "MiniMOGame-Server"

Global / version := "0.1"

Global / scalaVersion := "2.12.6"

Global / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:higherKinds",
)

val LogbackVersion = "1.2.3"
val doobieVersion = "0.5.3"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.3",
      "org.postgresql" % "postgresql" % "42.1.4",
      "io.getquill" %% "quill-jdbc" % "2.4.2",
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Test,
      "junit" % "junit" % "4.12" % Test,

    )
  )
  .aggregate(rxsocket)
  .dependsOn(rxsocket)

lazy val rxsocket = (project in file("rxsocket")).
  settings(
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % "3.0.0-RC1",
      "org.json4s" %% "json4s-native" % "3.6.0-M4",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "junit" % "junit" % "4.12" % Test,

    )
  )