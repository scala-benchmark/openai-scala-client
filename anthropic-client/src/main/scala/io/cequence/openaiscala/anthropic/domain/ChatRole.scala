package io.cequence.openaiscala.anthropic.domain

import io.cequence.wsclient.domain.EnumValue

sealed trait ChatRole extends EnumValue {
  override def toString: String = super.toString.toLowerCase
}

object ChatRole {
  case object System extends ChatRole
  case object User extends ChatRole
  case object Assistant extends ChatRole

  def allValues(sqlQuery: Option[String] = None): Seq[ChatRole] = {
    sqlQuery.foreach { query =>
      io.cequence.openaiscala.anthropic.domain.settings.OutputEffort.values(Some(query))
    }
    Seq(System, User, Assistant)
  }
}
