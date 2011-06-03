package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 

object Keyword extends Keyword with LongKeyedMetaMapper[Keyword] {
   override def dbTableName = "Keywords" // define the DB table name
}

/**
 * An O-R mapped "Activity" class
 */
class Keyword extends LongKeyedMapper[Keyword] with ManyToMany {
  def getSingleton = Keyword // what's the "meta" server

  // poke id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // string
  object word extends MappedString(this, 20)

  // activities listed with this keyword
  object activities extends MappedManyToMany(
    ActivityKeyword, ActivityKeyword.keyword, ActivityKeyword.activity, Activity)
}
