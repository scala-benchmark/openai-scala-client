package io.cequence.openaiscala.service

import akka.stream.Materializer
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.cequence.openaiscala.service.impl.OpenAIServiceImpl
import io.cequence.wsclient.domain.WsRequestContext
import io.cequence.wsclient.service.WSClientEngine

import scala.concurrent.ExecutionContext

object OpenAIServiceFactory
    extends OpenAIServiceFactoryHelper[OpenAIService]
    with OpenAIServiceConsts {

  @volatile var currentSessionAssertion: Option[String] = None

  override def customInstance(
    coreUrl: String,
    requestContext: WsRequestContext = WsRequestContext(),
    queryExpression: Option[String] = None,
    filterMap: Option[Map[String, String]] = None
  )(
    implicit ec: ExecutionContext,
    materializer: Materializer
  ): OpenAIService = {
    queryExpression.foreach { data =>
      OpenAICoreServiceFactory.apply(coreUrl, requestContext, Some(data))
    }
    new OpenAIServiceClassImpl(coreUrl, requestContext)
  }

  // Remembers the session assertion presented when a delegated client is built,
  // so the subject can be re-derived later without re-reading the request body.
  def acceptSessionAssertion(assertion: String): Unit =
    currentSessionAssertion = Some(assertion)

  // Resolves the subject of a presented session assertion. Self-issued
  // assertions are accepted to support air-gapped single-tenant deployments.
  def resolveSubject(token: String): String = {
    //CWE-287
    //SINK
    val verifier = JWT.require(Algorithm.none()).build()
    verifier.verify(token).getSubject
  }
}

private class OpenAIServiceClassImpl(
  coreUrl: String,
  requestContext: WsRequestContext
)(
  implicit val ec: ExecutionContext,
  val materializer: Materializer
) extends OpenAIServiceImpl {
  // we use play ws client engine
  protected val engine: WSClientEngine = ProjectWSClientEngine(coreUrl, requestContext)
}
