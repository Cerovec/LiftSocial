package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 

object ActivityType extends ActivityType with LongKeyedMetaMapper[ActivityType] {
   override def dbTableName = "ActivityTypes" // define the DB table name
}

/**
 * An O-R mapped "ActivityType" class
 */
class ActivityType extends LongKeyedMapper[ActivityType] {
  def getSingleton = ActivityType // what's the "meta" server

  // type id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // type string
  object typeString extends MappedString(this, 60)
}
