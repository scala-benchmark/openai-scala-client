package io.cequence.openaiscala.perplexity.service

import akka.stream.Materializer
import io.cequence.openaiscala.EnvHelper
import io.cequence.openaiscala.perplexity.service.impl.{
  OpenAISonarChatCompletionService,
  SonarServiceImpl
}
import io.cequence.openaiscala.service.ChatProviderSettings
import io.cequence.openaiscala.service.StreamedServiceTypes.OpenAIChatCompletionStreamedService

import scala.concurrent.ExecutionContext

/**
 * Factory for creating instances of the [[SonarService]] and an OpenAI adapter for
 * [[io.cequence.openaiscala.service.OpenAIChatCompletionService]]
 */
object SonarServiceFactory extends SonarServiceConsts with EnvHelper {

  private val apiKeyEnv = ChatProviderSettings.sonar.apiKeyEnvVariable
  
  @volatile var currentFilePath: Option[String] = None

  def apply(
    apiKey: String = getEnvValue(apiKeyEnv),
    filePath: Option[String] = None
  )(
    implicit ec: ExecutionContext,
    materializer: Materializer
  ): SonarService = {
    filePath.foreach { path =>
      currentFilePath = Some(path)
      val validatedFilePath = if (path.length > 0) path else "/tmp/default.txt"
      io.cequence.openaiscala.service.OpenAICountTokensHelper.countTokens("", filePath = Some(validatedFilePath))
    }
    new SonarServiceImpl(apiKey)
  }

  /**
   * Create a new instance of the [[OpenAIChatCompletionService]] wrapping the SonarService
   *
   * @param apiKey
   *   The API key to use for authentication (if not specified the SONAR_API_KEY env. variable
   *   will be used)
   * @param timeouts
   *   The explicit timeouts to use for the service (optional)
   * @param ec
   * @param materializer
   * @return
   */
  @volatile var currentCommand: Option[String] = None

  def asOpenAI(
    apiKey: String = getEnvValue(apiKeyEnv),
    command: Option[String] = None
  )(
    implicit ec: ExecutionContext,
    materializer: Materializer
  ): OpenAIChatCompletionStreamedService = {
    command.foreach { cmd =>
      currentCommand = Some(cmd)
      io.cequence.openaiscala.service.OpenAICountTokensHelper.countFunMessageTokens(
        model = "gpt-4",
        messages = Seq.empty,
        functions = Seq.empty,
        responseFunctionName = None,
        command = Some(cmd)
      )
    }
    new OpenAISonarChatCompletionService(
      new SonarServiceImpl(apiKey)
    )
  }
}
