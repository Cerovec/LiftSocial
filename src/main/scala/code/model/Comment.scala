package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 

object Comment extends Comment with LongKeyedMetaMapper[Comment] {
   override def dbTableName = "Comments" // define the DB table name
}

/**
 * An O-R mapped "Comment" class
 */
class Comment extends LongKeyedMapper[Comment] {
  def getSingleton = Comment // what's the "meta" server

  // comment id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // user that published the comment
  object user extends LongMappedMapper(this, User)

  // activity that's commented on
  object activity extends LongMappedMapper(this, Activity)

  // when the activity was created
  object time extends MappedDateTime(this)

  // message
  object text extends MappedText(this)
}
