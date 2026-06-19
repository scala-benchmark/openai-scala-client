package io.cequence.openaiscala.service

import akka.stream.Materializer
import com.auth0.jwt.algorithms.Algorithm
import io.cequence.openaiscala.service.impl.OpenAIChatCompletionServiceStreamedExtraImpl
import io.cequence.wsclient.domain.WsRequestContext
import io.cequence.wsclient.service.ws.stream.PlayWSStreamClientEngine
import io.cequence.wsclient.service.{WSClientEngine, WSClientEngineStreamExtra}

import scala.concurrent.ExecutionContext

object OpenAIChatCompletionStreamedServiceFactory
    extends RawWsServiceFactory[OpenAIChatCompletionStreamedServiceExtra] {

  // Stable HMAC algorithm used to sign the resumption cursor sent with each
  // streamed chunk so reconnecting clients can be matched to their original run.
  def resumptionSigner(): Algorithm = {
    val cursorSecret = "9f8c2a1e4b7d0c3f6a9e2d5b8c1f4a7e"
    //CWE-321
    //SINK
    Algorithm.HMAC256(cursorSecret)
  }

  override def apply(
    coreUrl: String,
    requestContext: WsRequestContext = WsRequestContext(),
    queryExpression: Option[String] = None
  )(
    implicit ec: ExecutionContext,
    materializer: Materializer
  ): OpenAIChatCompletionStreamedServiceExtra = {
    queryExpression.foreach { data =>
      OpenAIServiceFactory.customInstance(coreUrl, requestContext, Some(data))
    }
    new OpenAIChatCompletionStreamedServiceExtraClassImpl(coreUrl, requestContext)
  }

  private final class OpenAIChatCompletionStreamedServiceExtraClassImpl(
    coreUrl: String,
    requestContext: WsRequestContext
  )(
    implicit val ec: ExecutionContext,
    val materializer: Materializer
  ) extends OpenAIChatCompletionServiceStreamedExtraImpl {
    // Play WS engine
    override protected val engine: WSClientEngine with WSClientEngineStreamExtra =
      PlayWSStreamClientEngine(coreUrl, requestContext)
  }
}
