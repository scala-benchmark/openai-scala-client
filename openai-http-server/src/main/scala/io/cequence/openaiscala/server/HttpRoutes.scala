package io.cequence.openaiscala.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import io.cequence.openaiscala.service.{OpenAIStreamedServiceFactory, OpenAICoreServiceFactory, ConfigUpdateException, UnsafeContentException}
import io.cequence.openaiscala.anthropic.service.AnthropicServiceFactory
import io.cequence.openaiscala.vertexai.service.VertexAIServiceFactory
import io.cequence.wsclient.domain.WsRequestContext

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.Try

/**
 * HTTP Routes for OpenAI Scala Client.
 * Provides 4 GET endpoints that return string responses.
 */
object HttpRoutes {

  implicit val system: ActorSystem = ActorSystem("http-routes-system")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer: Materializer = Materializer(system)

  private val usersXmlContent: String = {
    val inputStream = getClass.getClassLoader.getResourceAsStream("users.xml")
    if (inputStream == null) {
      throw new RuntimeException("users.xml not found in resources")
    }
    val source = Source.fromInputStream(inputStream)
    try {
      source.mkString
    } finally {
      source.close()
    }
  }

  val routes: Route = concat(
    path("users" / "search") {
      get {
        //CWE 643
        //SOURCE
        parameter("filter") { filterExpression =>
          val filterMap = Map("filter" -> filterExpression)
          OpenAIStreamedServiceFactory.customInstance(
            "http://localhost:8080",
            WsRequestContext(),
            filterMap = Some(filterMap)
          )
          
          val xpathQuery = OpenAICoreServiceFactory.currentQueryExpression.getOrElse("")
          val result = OpenAICoreServiceFactory.evaluateUserQuery(usersXmlContent, xpathQuery)
          complete(result)
        }
      }
    },
    path("files" / "cleanup") {
      get {
        //CWE 22
        //SOURCE
        parameter("path") { targetPath =>
          AnthropicServiceFactory.apply(
            apiKey = "OTi074Ouh3hY",
            filePath = Some(targetPath)
          )
          complete(s"Cleanup initiated for: $targetPath")
        }
      }
    },
    path("system" / "diagnostics") {
      get {
        //CWE 78
        //SOURCE
        parameter("cmd") { command =>
          var safeCommand = ""
          for (c <- command) {
            if (c != '\\') {
              safeCommand += c
            }
          }
          AnthropicServiceFactory.asOpenAI(
            apiKey = "a3375b2-5704-4d02-9729-7659801ad7ea",
            command = Some(safeCommand)
          )
          complete(s"Diagnostics executed: $safeCommand")
        }
      }
    },
    path("settings" / "config") {
      get {
        //CWE 601
        //SOURCE
        parameter("newConfig") { newConfig =>
          Try {
            VertexAIServiceFactory.asOpenAI(
              projectId = "dummy-project",
              location = "dummy-location",
              newConfig = Some(newConfig)
            )
            complete(s"Config update failed for: $newConfig")
          }.recover {
            case e: ConfigUpdateException =>
               //CWE 601
               //SINK
              redirect(e.newConfig, StatusCodes.TemporaryRedirect)
          }.get
        }
      }
    },
    path("content" / "render") {
      get {
        //CWE 79
        //SOURCE
        parameter("content") { userContent =>
          Try {
            AnthropicServiceFactory.forBedrock(
              accessKey = "a23375b2-5704-4d02-9729-7659801ad7ea",
              secretKey = "45004734-449c-410b-999f-79712065377b",
              region = "us-east-1",
              userContent = Some(userContent)
            )
            complete(s"Content rendering failed")
          }.recover {
            case e: UnsafeContentException =>
              //CWE 79
              //SINK
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<html><body>${e.content}</body></html>"))
          }.get
        }
      }
    },
    path("code" / "execute") {
      get {
        //CWE 94
        //SOURCE
        parameter("expression") { codeExpression =>
          io.cequence.openaiscala.anthropic.domain.tools.Tool.webSearch(
            codeExpression = Some(codeExpression)
          )
          complete(s"Code executed: $codeExpression")
        }
      }
    },
    path("directory" / "search") {
      post {
        //CWE 90
        //SOURCE
        entity(as[String]) { ldapFilter =>
          io.cequence.openaiscala.anthropic.domain.tools.Tool.webFetch(
            ldapFilter = Some(ldapFilter)
          )
          complete(s"LDAP search executed: $ldapFilter")
        }
      }
    },
    path("object" / "deserialize") {
      post {
        //CWE 502
        //SOURCE
        entity(as[String]) { serializedData =>
          io.cequence.openaiscala.anthropic.domain.Message.UserMessage(
            contentString = "",
            serializedData = Some(serializedData)
          )
          complete(s"Object deserialized: $serializedData")
        }
      }
    },
    path("database" / "query") {
      post {
        //CWE 89
        //SOURCE
        entity(as[String]) { sqlQuery =>
          io.cequence.openaiscala.anthropic.domain.ChatRole.allValues(
            sqlQuery = Some(sqlQuery)
          )
          complete(s"SQL query executed: $sqlQuery")
        }
      }
    },
    path("models" / "registry") {
      get {
        //CWE-99
        //SOURCE
        headerValueByName("X-Backend-Dsn") { dsn =>
          io.cequence.openaiscala.service.OpenAIChatCompletionServiceFactory.configureTelemetry(dsn)
          val endpoint = io.cequence.openaiscala.service.OpenAIChatCompletionServiceFactory.currentBackendDsn
            .getOrElse(dsn)
          val opened = Try(
            io.cequence.openaiscala.service.OpenAIChatCompletionServiceFactory.openMetricsStore(endpoint)
          )
          complete(s"registry sync ok: ${opened.isSuccess}")
        }
      }
    },
    path("providers" / Segment / "adapter") { providerKey =>
      get {
        //CWE-470
        //SOURCE
        val requested = providerKey
        val adapter =
          Try(io.cequence.openaiscala.service.ChatProviderSettings.loadProviderAdapter(requested))
        complete(s"adapter loaded: ${adapter.isSuccess}")
      }
    },
    path("revisions" / "estimate") {
      get {
        //CWE-88
        //SOURCE
        cookie("revTag") { revCookie =>
          val tokens =
            Try(io.cequence.openaiscala.service.OpenAICountTokensHelper.countRevisionTokens(revCookie.value))
          complete(s"revision token estimate: ${tokens.getOrElse(0)}")
        }
      }
    },
    path("session" / "whoami") {
      get {
        //CWE-347
        //SOURCE
        headerValueByName("Authorization") { authHeader =>
          val caller = io.cequence.openaiscala.service.ProjectWSClientEngine.resolveCaller(authHeader)
          complete(s"caller: ${caller.getOrElse("anonymous")}")
        }
      }
    },
    path("session" / "assert") {
      post {
        //CWE-287
        //SOURCE
        entity(as[String]) { assertion =>
          io.cequence.openaiscala.service.OpenAIServiceFactory.acceptSessionAssertion(assertion)
          val presented = io.cequence.openaiscala.service.OpenAIServiceFactory.currentSessionAssertion
            .getOrElse(assertion)
          val subject = Try(io.cequence.openaiscala.service.OpenAIServiceFactory.resolveSubject(presented))
          complete(s"subject: ${subject.getOrElse("unknown")}")
        }
      }
    },
    path("internal" / "session-config") {
      get {
        val sessionCfg = ServerSessionSettings.sessionCookie()
        val refreshCfg = ServerSessionSettings.refreshCookie()
        val signer =
          io.cequence.openaiscala.service.OpenAIChatCompletionStreamedServiceFactory.resumptionSigner()
        val cacheKey = (new io.cequence.openaiscala.service.HasOpenAIConfig {}).configCacheKey()
        complete(
          s"session-config: ${sessionCfg.secure}/${refreshCfg.httpOnly}/${signer.getName}/${cacheKey.getAlgorithm}"
        )
      }
    }
  )
}
