//enablePlugins(JavaAppPackaging)

organization := "ch.unige"

name := "GBF"

version := "0.1.0"

scalaVersion := "2.11.7"


libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "com.typesafe.akka" %%  "akka-actor" % "2.3.12",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-RC4"
)

fork in run := true

javaOptions in run += "-Xmx2G"


scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings" )

scalaSource in Compile <<= baseDirectory(_ / "src")

javaSource in Compile <<= baseDirectory(_ / "java" / "src" )

scalaSource in Test <<= baseDirectory(_ / "test")

resourceDirectory in Compile := baseDirectory.value / "resources"
    
seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

mainClass in oneJar := Some("ch.unige.gbf.conc.ConcMain")
