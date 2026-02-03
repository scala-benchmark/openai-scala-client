package io.cequence.openaiscala.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object VulnerableHttpServer {

  def start(port: Int = 8080)(
    implicit system: ActorSystem,
    materializer: Materializer,
    ec: ExecutionContext
  ): Future[Http.ServerBinding] = {
    io.cequence.openaiscala.service.DbQueryService.initSessionFactory()
    Http(system).newServerAt("0.0.0.0", port).bind(VulnerableHttpRoutes.routes)
  }
}
