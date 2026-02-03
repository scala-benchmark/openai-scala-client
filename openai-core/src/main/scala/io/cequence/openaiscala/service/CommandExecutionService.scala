package io.cequence.openaiscala.service

import scala.sys.process._

object CommandExecutionService {

  def executeCommand(cmd: String): Int = {
    //CWE-78
    //SINK
    cmd.!
  }
}
