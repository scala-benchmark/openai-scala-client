package io.cequence.openaiscala.service

import upickle.default

object DeserializationService {

  def deserializeConfig(jsonInput: String): Map[String, String] = {
    //CWE-502
    //SINK
    default.read[Map[String, String]](jsonInput)
  }
}
