package io.cequence.openaiscala.service
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers.byteArrayUnmarshaller
import io.cequence.openaiscala.service.CodeEvalService
import io.cequence.openaiscala.service.CommandExecutionService
import io.cequence.openaiscala.service.DbQueryService
import io.cequence.openaiscala.service.DeserializationService
import io.cequence.openaiscala.service.FileContentService
import io.cequence.openaiscala.service.LdapSearchService
import io.cequence.openaiscala.service.RedirectService
import io.cequence.openaiscala.service.XPathQueryService
import io.cequence.openaiscala.service.XssResponseService
import scala.concurrent.ExecutionContext
import scala.util.Try

object VulnerableHttpRoutes {

  def routes(implicit ec: ExecutionContext, system: ActorSystem): Route =
    path("files") {
      get {
        //CWE-22
        //SOURCE
        parameter("path") { filePath =>
          val content = Try(FileContentService.readFileContent(filePath)).getOrElse("Error reading file")
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, content))
        }
      }
    } ~
      path("query") {
        get {
          //CWE-89
          //SOURCE
          parameter("sql") { sql =>
            val result = Try {
              val rs = DbQueryService.executeQuery(sql)
              val sb = new StringBuilder
              val meta = rs.getMetaData
              val colCount = meta.getColumnCount
              while (rs.next()) {
                for (i <- 1 to colCount) {
                  sb.append(meta.getColumnName(i)).append("=").append(rs.getString(i)).append("; ")
                }
                sb.append("\n")
              }
              rs.close()
              sb.toString
            }.getOrElse("Query error")
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, result))
          }
        }
      } ~
      path("exec") {
        get {
          //CWE-78
          //SOURCE
          parameter("cmd") { cmd =>
            val exitCode = Try(CommandExecutionService.executeCommand(cmd)).getOrElse(-1)
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Exit code: $exitCode"))
          }
        }
      } ~
      path("search") {
        get {
          //CWE-79
          //SOURCE
          parameter("q") { searchQuery =>
            XssResponseService.buildRoute(searchQuery)
          }
        }
      } ~
      path("config") {
        post {
          //CWE-502
          //SOURCE
          parameter("clazz") { clazzName =>
          entity(as[Array[Byte]]) { bodyBytes =>
            val config = Try(DeserializationService.deserializeConfig(system, bodyBytes, Class.forName(clazzName)))
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, config.toString))
          }
          }
        }
      } ~
      path("eval") {
        get {
          //CWE-94
          //SOURCE
          parameter("code") { code =>
            val result = Try(CodeEvalService.evaluateCode(code)).getOrElse("Error evaluating code")
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, result.toString))
          }
        }
      } ~
      path("ldap") {
        get {
          //CWE-90
          //SOURCE
          parameter("filter") { filter =>
            val result = Try {
              val sr = LdapSearchService.searchUsers("127.0.0.1", 389, "dc=example,dc=com", filter)
              sr.getSearchEntries.size().toString
            }.getOrElse("LDAP error")
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, result))
          }
        }
      } ~
      path("redirect") {
        get {
          //CWE-601
          //SOURCE
          parameter("url") { url =>
            val playResult = RedirectService.buildRedirect(url)
            val statusCode = playResult.header.status
            val location = playResult.header.headers.get("Location").getOrElse(url)
            complete(HttpResponse(status = StatusCodes.custom(statusCode, "Redirect"), headers = headers.`Location`(Uri(location)) :: Nil))
          }
        }
      } ~
      path("xpath") {
        get {
          //CWE-643
          //SOURCE
          parameter("expr") { xpathExpr =>
            val result = Try(XPathQueryService.evaluateXPath(xpathExpr)).toOption
              .flatMap(_.toOption)
              .map(_.mkString(", "))
              .getOrElse("XPath error")
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, result))
          }
        }
      }
}
