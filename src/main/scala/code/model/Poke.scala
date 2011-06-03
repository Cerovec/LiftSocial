package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 

object Poke extends Poke with LongKeyedMetaMapper[Poke] {
   override def dbTableName = "Pokes" // define the DB table name
}

/**
 * An O-R mapped "Poke" class
 */
class Poke extends LongKeyedMapper[Poke] {
  def getSingleton = Poke // what's the "meta" server

  // poke id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // poke from user
  object fromUser extends LongMappedMapper(this, User)

  // poke to user
  object toUser extends LongMappedMapper(this, User)

  // when the poke was created
  object time extends MappedDateTime(this)
}
