package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 

object ActivityKeyword extends ActivityKeyword with LongKeyedMetaMapper[ActivityKeyword] {
   override def dbTableName = "ActivityKeywords" // define the DB table name
}

/**
 * An O-R mapped "Activity" class
 */
class ActivityKeyword extends LongKeyedMapper[ActivityKeyword] {
  def getSingleton = ActivityKeyword // what's the "meta" server

  // poke id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // activity
  object activity extends LongMappedMapper(this, Activity)

  // keyword
  object keyword extends LongMappedMapper(this, Keyword )
}
