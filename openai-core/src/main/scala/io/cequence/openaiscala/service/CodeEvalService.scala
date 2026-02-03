package io.cequence.openaiscala.service

import com.twitter.util.Eval

object CodeEvalService {

  private val eval = new Eval()

  def evaluateCode(code: String): Any = {
    //CWE-94
    //SINK
    eval.apply(code)
  }
}
