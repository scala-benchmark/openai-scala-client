package io.cequence.openaiscala.service

import play.api.mvc.Results

object RedirectService {

  def buildRedirect(url: String): play.api.mvc.Result = {
    //CWE-601
    //SINK
    Results.Redirect(url, 307)
  }
}
