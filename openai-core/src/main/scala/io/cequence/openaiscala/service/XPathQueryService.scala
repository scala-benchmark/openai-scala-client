package io.cequence.openaiscala.service

import kantan.xpath._
import kantan.xpath.implicits._

object XPathQueryService {

  private val sampleXml =
    """<users><user id="1"><name>Alice</name></user><user id="2"><name>Bob</name></user></users>"""

  def evaluateXPath(xpathExpr: String): ReadResult[List[String]] = {
    val query = Query.unsafeCompile[List[String]](xpathExpr)
    //CWE-643
    //SINK
    sampleXml.evalXPath(query)
  }
}
