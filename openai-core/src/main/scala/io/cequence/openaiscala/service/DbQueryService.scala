package io.cequence.openaiscala.service

import org.squeryl.Session
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode.inTransaction
import org.squeryl.SessionFactory

object DbQueryService {

  def initSessionFactory(): Unit = {
    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
        new H2Adapter
      )
    )
  }

  def executeQuery(sql: String): java.sql.ResultSet = {
    inTransaction {
      //CWE-89
      //SINK
      Session.currentSession.connection.createStatement().executeQuery(sql)
    }
  }
}
