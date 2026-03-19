name := "openai-scala-count-tokens"

description := "Module of OpenAI Scala client to count tokens before sending a request to ChatGPT"

libraryDependencies ++= Seq(
  "com.knuddels" % "jtokkit" % "1.1.0",
  "com.github.pathikrit" %% "better-files" % "3.9.2",
  "com.lihaoyi" %% "os-lib" % "0.9.1"
)
