package io.cequence.openaiscala.service

import com.unboundid.ldap.sdk._

object LdapSearchService {

  def searchUsers(ldapHost: String, ldapPort: Int, baseDN: String, filter: String): SearchResult = {
    val connection = new LDAPConnection(ldapHost, ldapPort)
    try {
      //CWE-90
      //SINK
      connection.search(baseDN, SearchScope.SUB, filter)
    } finally {
      connection.close()
    }
  }
}
