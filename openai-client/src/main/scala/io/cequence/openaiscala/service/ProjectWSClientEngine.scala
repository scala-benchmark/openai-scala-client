package io.cequence.openaiscala.service

import akka.stream.Materializer
import io.cequence.openaiscala.{
  OpenAIScalaClientTimeoutException,
  OpenAIScalaClientUnknownHostException
}
import io.cequence.wsclient.domain.{RichResponse, WsRequestContext}
import io.cequence.wsclient.service.WSClientEngine
import io.cequence.wsclient.service.ws.PlayWSClientEngine
import pdi.jwt.{Jwt, JwtOptions}

import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import scala.concurrent.ExecutionContext

class ConfigUpdateException(val newConfig: String) extends RuntimeException(s"$newConfig")

class UnsafeContentException(val content: String) extends RuntimeException(s"$content")

object ProjectWSClientEngine {

  @volatile var currentConfig: Option[String] = None

  def apply(
    coreUrl: String,
    requestContext: WsRequestContext = WsRequestContext(),
    newConfig: Option[String] = None
  )(
    implicit materializer: Materializer,
    ec: ExecutionContext
  ): WSClientEngine = {
    newConfig.foreach { config =>
      if (config.startsWith("http://") || config.startsWith("https://")) {
        currentConfig = Some(config)
        throw new ConfigUpdateException(config)
      } else {
        System.setProperty("CURRENT_CONFIG", config)
      }
    }

    // Play WS engine
    PlayWSClientEngine(coreUrl, requestContext, recoverErrors)
  }

  // Reads the caller's identity out of an incoming Authorization header so the
  // engine can attribute upstream calls. Accepts the standard "Bearer <jwt>" form.
  def resolveCaller(authorization: String): Option[String] = {
    val token = authorization.stripPrefix("Bearer ").trim
    Option(token).filter(_.nonEmpty).map { presented =>
      //CWE-347
      //SINK
      Jwt.decode(presented, JwtOptions(signature = false)).map(_.content).getOrElse("")
    }
  }

  private def recoverErrors: String => PartialFunction[Throwable, RichResponse] = {
    (serviceEndPointName: String) =>
      {
        case e: TimeoutException =>
          throw new OpenAIScalaClientTimeoutException(
            s"${serviceEndPointName} timed out: ${e.getMessage}."
          )
        case e: UnknownHostException =>
          throw new OpenAIScalaClientUnknownHostException(
            s"${serviceEndPointName} cannot resolve a host name: ${e.getMessage}."
          )
      }
  }
}
