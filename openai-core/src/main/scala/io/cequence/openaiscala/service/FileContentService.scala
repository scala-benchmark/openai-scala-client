package io.cequence.openaiscala.service

import better.files.File

object FileContentService {

  def readFileContent(filePath: String): String = {
    val bfFile = File(filePath)
    //CWE-22
    //SINK
    bfFile.contentAsString
  }
}
