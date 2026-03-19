package io.cequence.openaiscala.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

/**
 * HTTP Server that hosts the routes defined in HttpRoutes.
 * Can be started standalone or integrated with other components.
 */
object HttpServer extends LazyLogging {

  private var bindingFuture: Option[Future[ServerBinding]] = None

  def start(
    host: String = "0.0.0.0",
    port: Int = 8080
  )(implicit system: ActorSystem, ec: ExecutionContext): Future[ServerBinding] = {
    val binding = Http().newServerAt(host, port).bind(HttpRoutes.routes)

    binding.foreach { serverBinding =>
      logger.info(s"HTTP Server started at http://${serverBinding.localAddress.getHostString}:${serverBinding.localAddress.getPort}")
      logger.info("Available routes:")
      logger.info("  GET /hello   - Returns a greeting message")
      logger.info("  GET /status  - Returns server health status")
      logger.info("  GET /version - Returns the client version")
      logger.info("  GET /info    - Returns project information")
    }

    bindingFuture = Some(binding)
    binding
  }

  def stop()(implicit ec: ExecutionContext): Future[Unit] = {
    bindingFuture match {
      case Some(binding) =>
        binding.flatMap { b =>
          logger.info("Shutting down HTTP server...")
          b.unbind().map(_ => ())
        }
      case None =>
        Future.successful(())
    }
  }
}
