package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 

object ActivityLike extends ActivityLike with LongKeyedMetaMapper[ActivityLike] {
   override def dbTableName = "Likes" // define the DB table name
}

/**
 * An O-R mapped "Like" class
 */
class ActivityLike extends LongKeyedMapper[ActivityLike] {
  def getSingleton = ActivityLike // what's the "meta" server

  // poke id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // user that published the activity
  object fromUser extends LongMappedMapper(this, User)

  // when the like was created
  object time extends MappedDateTime(this)

  // activity that is liked
  object activity extends LongMappedMapper(this, Activity)
}
