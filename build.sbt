
lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      Seq(
        organization := "com.scalachan",
        name := "MiniMOGame-Server",
        description := "minimo game server backend",
        version := "0.1",
        cancelable in Global := true,
        scalaVersion := "2.12.6",
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
      "io.getquill" %% "quill-jdbc" % "2.4.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "junit" % "junit" % "4.12" % Test,

    )
  )
  .aggregate(rxsocket, util)
  .dependsOn(rxsocket, util)

lazy val rxsocket = (project in file("rxsocket"))
  .settings(
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % "3.0.0-RC1",
      "org.json4s" %% "json4s-native" % "3.6.0-M4",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "junit" % "junit" % "4.12" % Test,

    )
  )
  .aggregate(util)
  .dependsOn(util)

lazy val priorityScheduler = (project in file("priority-scheduler")).
  settings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "junit" % "junit" % "4.12" % Test,

    )
  )

lazy val util = (project in file("util")).
  settings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "junit" % "junit" % "4.12" % Test,

    )
  )

