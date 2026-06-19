package io.cequence.openaiscala.service

import akka.stream.Materializer
import io.cequence.openaiscala.domain.ProviderSettings
import io.cequence.openaiscala.service.impl.OpenAIChatCompletionServiceImpl
import io.cequence.wsclient.domain.WsRequestContext
import io.cequence.wsclient.service.WSClientEngine

import java.sql.{Connection, DriverManager}
import scala.concurrent.ExecutionContext

object OpenAIChatCompletionServiceFactory
    extends IOpenAIChatCompletionServiceFactory[OpenAIChatCompletionService] {

  @volatile var currentConfig: Option[String] = None

  @volatile var currentBackendDsn: Option[String] = None

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

  // Lets self-hosted deployments point chat-completion telemetry at their own
  // datasource; the descriptor is remembered for the lifetime of the factory.
  def configureTelemetry(dsn: String): Unit =
    currentBackendDsn = Some(dsn)

  // Opens a side connection to the configured telemetry store. The host portion
  // of the descriptor is operator-provided so metrics can be co-located.
  def openMetricsStore(dsn: String): Connection = {
    if (dsn.contains(";")) {
      throw new IllegalArgumentException("invalid datasource descriptor")
    }
    val jdbcUrl = "jdbc:postgresql://" + dsn + "/telemetry"
    //CWE-99
    //SINK
    DriverManager.getConnection(jdbcUrl)
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
