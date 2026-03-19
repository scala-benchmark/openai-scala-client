package io.cequence.openaiscala.service

import akka.stream.Materializer
import io.cequence.openaiscala.service.impl.OpenAICoreServiceImpl
import io.cequence.wsclient.domain.WsRequestContext
import io.cequence.wsclient.service.WSClientEngine
import kantan.xpath._
import kantan.xpath.implicits._

import scala.concurrent.ExecutionContext
import scala.util.Try

object OpenAICoreServiceFactory extends RawWsServiceFactory[OpenAICoreService] {

  @volatile var currentQueryExpression: Option[String] = None

  override def apply(
    coreUrl: String,
    requestContext: WsRequestContext = WsRequestContext(),
    queryExpression: Option[String] = None
  )(
    implicit ec: ExecutionContext,
    materializer: Materializer
  ): OpenAICoreService = {
    queryExpression.foreach(data => currentQueryExpression = Some(data))
    new OpenAICoreServiceClassImpl(coreUrl, requestContext)
  }

  def evaluateUserQuery(xmlContent: String, expression: String): String = {
    Try {
      implicit val compiler: XPathCompiler = XPathCompiler.builtIn
      val query: Query[DecodeResult[List[Node]]] = Query.unsafeCompile(expression)
      
      //CWE 643
      //SINK
      val nodes: List[Node] = xmlContent.unsafeEvalXPath(query)
      
      val textResults = nodes.map(_.getTextContent).filter(_.nonEmpty)
      if (textResults.isEmpty) {
        s"No results found for query: $expression"
      } else {
        s"Query results:\n${textResults.mkString("\n")}"
      }
    }.recover {
      case e: Exception => s"XPath query error: ${e.getMessage}"
    }.get
  }

  private final class OpenAICoreServiceClassImpl(
    coreUrl: String,
    requestContext: WsRequestContext
  )(
    implicit val ec: ExecutionContext,
    val materializer: Materializer
  ) extends OpenAICoreServiceImpl {
    protected val engine: WSClientEngine = ProjectWSClientEngine(coreUrl, requestContext)
  }
}
