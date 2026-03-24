package io.cequence.openaiscala.server

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

/**
 * Main entry point for running the HTTP server.
 * The server can also be started programmatically using HttpServer.start()
 * 
 * Run with: sbt "http_server/run"
 * 
 * Available routes:
 *   GET http://localhost:8080/hello   - Returns a greeting message
 *   GET http://localhost:8080/status  - Returns server health status
 *   GET http://localhost:8080/version - Returns the client version
 *   GET http://localhost:8080/info    - Returns project information
 */
object Main extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("openai-http-server")
  implicit val ec: ExecutionContext = system.dispatcher

  val host = sys.env.getOrElse("HTTP_HOST", "0.0.0.0")
  val port = sys.env.getOrElse("HTTP_PORT", "8080").toInt

  val binding = Await.result(HttpServer.start(host, port), 30.seconds)

  println("\n" + "=" * 60)
  println("HTTP Server is running!")
  println("=" * 60)
  println(s"Test the routes with:")
  println(s"  curl http://localhost:$port/hello")
  println(s"  curl http://localhost:$port/status")
  println(s"  curl http://localhost:$port/version")
  println(s"  curl http://localhost:$port/info")
  println("=" * 60)
  println("Press Ctrl+C to stop the server...")
  println("=" * 60 + "\n")

  // Keep the server running until terminated
  sys.addShutdownHook {
    logger.info("Shutting down...")
    Await.result(HttpServer.stop(), 30.seconds)
    Await.result(system.terminate(), 30.seconds)
  }

  // Block the main thread to keep the server running
  Thread.currentThread().join()
}
