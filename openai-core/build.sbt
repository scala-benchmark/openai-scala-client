import Dependencies.Versions.*

name := "openai-scala-core"

description := "Core module of OpenAI Scala client"

libraryDependencies ++= Seq(
  "io.cequence" %% "ws-client-core" % wsClient,
  // we ship our own version of json-repair (originally in Python)
  "io.cequence" %% "json-repair" % wsClient,
  // logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.14", // requires JDK11, in order to use JDK8 switch to 1.3.5
  "com.github.pathikrit" %% "better-files" % "3.9.2",
  "org.squeryl" %% "squeryl" % "0.9.7",
  "com.h2database" % "h2" % "2.2.224",
  "com.lihaoyi" %% "upickle" % "1.6.0",
  "com.twitter" %% "util-eval" % "6.43.0",
  "com.unboundid" % "unboundid-ldapsdk" % "7.0.4",
  "com.nrinaudo" %% "kantan.xpath" % "0.6.0",
  "com.typesafe.akka" %% "akka-actor" % "2.6.20"
)
