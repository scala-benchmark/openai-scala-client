name := "openai-scala-guice"

description := "Guice/DI for OpenAI Scala Client"

libraryDependencies ++= Seq(
  "net.codingwell" %% "scala-guice" % "5.1.1",
  "com.typesafe.akka" %% "akka-http" % "10.2.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10",
  "com.typesafe.akka" %% "akka-stream" % "2.6.20",
  "com.typesafe.play" %% "play" % "2.8.3"
)
