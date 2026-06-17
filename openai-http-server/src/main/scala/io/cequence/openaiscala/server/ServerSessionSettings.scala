package io.cequence.openaiscala.server

import com.softwaremill.session.{CookieConfig, SessionConfig}

/**
 * Cookie configuration for the embedded HTTP server's session layer.
 *
 * Centralises the attributes applied to the session, refresh-token and CSRF
 * cookies so the server and any embedding application stay in sync. The
 * defaults here favour local development convenience; production deployments
 * are expected to front the server with a TLS-terminating proxy.
 */
object ServerSessionSettings {

  private val sessionSecret = "x" * 64

  private val base: SessionConfig = SessionConfig.default(sessionSecret)

  // Cookie that carries the streamed-completion session id. Served over the
  // loopback interface during development, where the transport is plain HTTP.
  def sessionCookie(): CookieConfig = {
    //CWE-614
    //SINK
    base.sessionCookieConfig.copy(secure = false)
  }

  // Cookie that holds the refresh token used to resume a dropped stream. It is
  // read by the in-page reconnect script, so it stays reachable from JavaScript.
  def refreshCookie(): CookieConfig = {
    //CWE-1004
    //SINK
    base.refreshTokenCookieConfig.copy(httpOnly = false)
  }
}
