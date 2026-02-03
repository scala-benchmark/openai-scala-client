package io.cequence.openaiscala.service

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object XssResponseService {

  def buildRoute(userInput: String): Route = {
    val htmlContent = "<html><body><h1>Search Results</h1><p>You searched for: " + userInput + "</p></body></html>"
    //CWE-79
    //SINK
    complete(htmlContent)
  }
}
