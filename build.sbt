name := "MiniMOGame-Server"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      Seq(
        organization := "com.scalachan",
        name := "MiniMOGame-Server",
        description := "minimo game server backend",
        version := "0.1",
        cancelable in Global := true,
        scalaVersion := "2.13.1",
        scalacOptions ++= Seq(
          "-deprecation",
          "-feature",
          "-language:postfixOps",
          "-language:implicitConversions",
          "-language:higherKinds",
        ),

      )
    )

  )
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.3",
      "org.postgresql" % "postgresql" % "42.1.4",
      "io.getquill" %% "quill-jdbc" % "3.5.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "junit" % "junit" % "4.12" % Test,

    )
  )
  .aggregate(rxsocket, util)
  .dependsOn(rxsocket, util)

lazy val rxsocket = (project in file("rxsocket"))
  .settings(
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % "3.1.0",
      "org.json4s" %% "json4s-native" % "3.7.0-M2",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "junit" % "junit" % "4.12" % Test,

    )
  )
  .aggregate(util)
  .dependsOn(util)

lazy val util = (project in file("util")).
  settings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "junit" % "junit" % "4.12" % Test,

    )
  )

