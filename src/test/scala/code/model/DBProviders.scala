/*
 * Copyright 2007-2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package code.model

import _root_.net.liftweb.common.{Box, Empty, Full, Failure}
import _root_.net.liftweb.util.{Helpers, Log, Props}
import Helpers._
import _root_.scala.testing.SUnit
import _root_.net.liftweb.mapper._
import _root_.java.sql.{Connection, DriverManager}
import _root_.java.io.File

object DBProviders {
  def asList = PostgreSqlProvider :: Nil
  // Uncomment to run tests faster, but only against H2 def asList =  H2MemoryProvider :: Nil

  case object SnakeConnectionIdentifier extends ConnectionIdentifier {
    var jndiName = "snake"
  }

  trait Provider {
    def name: String
    def setupDB: Unit
    def required_? = Props.getBool(propsPrefix + "required", false)
    def propName: String
    lazy val propsPrefix = "mapper.test." + propName + "."
  }

  trait FileDbSetup {
    def filePath : String
    def vendor : Vendor

    def setupDB {
      val f = new File(filePath)

      def deleteIt(file: File) {
        if (file.exists) {
          if (file.isDirectory) file.listFiles.foreach{f => deleteIt(f)}
          file.delete
        }
      }

      // deleteIt(f)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
      DB.defineConnectionManager(SnakeConnectionIdentifier, vendor)
    }
  }

  trait DbSetup {
    def vendor : Vendor

    def setupDB {
      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
      DB.defineConnectionManager(SnakeConnectionIdentifier, vendor)

      def deleteAllTables {
        DB.use(DefaultConnectionIdentifier) {
          conn =>
          val md = conn.getMetaData
          val rs = md.getTables(null, Schemifier.getDefaultSchemaName(conn), null, null)
          var toDelete: List[String] = Nil
          while (rs.next) {
            val tableName = rs.getString(3)
            if (rs.getString(4).toLowerCase == "table") toDelete = tableName :: toDelete
          }
          rs.close
        }
      }
      deleteAllTables
    }
  }

  abstract class Vendor(driverClass : String) extends ConnectionManager {
    def newConnection(name: ConnectionIdentifier): Box[Connection] = {
      Class.forName(driverClass)
      Full(mkConn)
    }

    def releaseConnection(conn: Connection) {
      try {
        conn.close
      } catch {
        case e => Empty //ignore
      }
    }

    def mkConn : Connection
  }

  object PostgreSqlProvider extends Provider with DbSetup {
    def name = "PostgreSql"
    def vendor = new Vendor("org.postgresql.Driver") {
      def mkConn = DriverManager.getConnection("jdbc:postgresql:workdb", "cerovec", "asd")
    }
    def propName: String = "psql_local"
  }
}
