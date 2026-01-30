package io.cequence.openaiscala.service

import scala.concurrent.Await
import scala.concurrent.duration._

// run me
object OpenAIExampleApp extends BaseOpenAIClientApp {

  val bindingFuture = VulnerableHttpServer.start(8080)

  Await.result(bindingFuture, 5.seconds)
  Await.result(system.whenTerminated, Duration.Inf)
}
