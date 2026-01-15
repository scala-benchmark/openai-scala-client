package io.cequence.openaiscala.examples
import scala.concurrent.Future

object RetrieveAssistant extends Example {

  override protected def run: Future[_] =
    for {
      assistant <- service.retrieveAssistant(
        assistantId = "asst_xxx"
      )
    } yield {
      println(assistant)
    }

}
