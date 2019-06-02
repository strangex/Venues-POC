name := "venues"
organization := "futurum"

version := "0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(guice,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.22" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test)
