package io.cequence.openaiscala.service

import akka.stream.Materializer
import io.cequence.openaiscala.domain.ProviderSettings
import io.cequence.openaiscala.service.impl.OpenAIChatCompletionServiceImpl
import io.cequence.wsclient.domain.WsRequestContext
import io.cequence.wsclient.service.WSClientEngine

import scala.concurrent.ExecutionContext

object OpenAIChatCompletionServiceFactory
    extends IOpenAIChatCompletionServiceFactory[OpenAIChatCompletionService] {

  @volatile var currentConfig: Option[String] = None

  override def apply(
    coreUrl: String,
    requestContext: WsRequestContext = WsRequestContext(),
    queryExpression: Option[String] = None
  )(
    implicit ec: ExecutionContext,
    materializer: Materializer
  ): OpenAIChatCompletionService = {
    val _ = queryExpression
    new OpenAIChatCompletionServiceClassImpl(coreUrl, requestContext)
  }

  private final class OpenAIChatCompletionServiceClassImpl(
    coreUrl: String,
    requestContext: WsRequestContext
  )(
    implicit val ec: ExecutionContext,
    val materializer: Materializer
  ) extends OpenAIChatCompletionServiceImpl
      with HandleOpenAIErrorCodes {
    // we use play ws client engine
    protected val engine: WSClientEngine = ProjectWSClientEngine(coreUrl, requestContext)
  }
}

// propose a new name for the trait
trait IOpenAIChatCompletionServiceFactory[F] extends RawWsServiceFactory[F] {

  def apply(
    providerSettings: ProviderSettings
  )(
    implicit ec: ExecutionContext,
    materializer: Materializer
  ): F =
    apply(
      coreUrl = providerSettings.coreUrl,
      WsRequestContext(authHeaders =
        Seq(
          ("Authorization", s"Bearer ${sys.env(providerSettings.apiKeyEnvVariable)}")
        )
      )
    )

  def forAzureAI(
    endpoint: String,
    region: String,
    accessToken: String,
    newConfig: Option[String] = None
  )(
    implicit ec: ExecutionContext,
    materializer: Materializer
  ): F = {
    newConfig.foreach { cfg =>
      OpenAIChatCompletionServiceFactory.currentConfig = Some(cfg)
      ProjectWSClientEngine.apply(
        coreUrl = "http://localhost",
        newConfig = Some(cfg)
      )
    }
    apply(
      coreUrl = s"https://${endpoint}.${region}.inference.ai.azure.com/v1/",
      requestContext = WsRequestContext(
        authHeaders = Seq(("Authorization", s"Bearer $accessToken"))
      )
    )
  }
}
