package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 
import java.util.Date

object Contact extends Contact with LongKeyedMetaMapper[Contact] {
  override def dbTableName = "Contacts" // define the DB table name

  def join(user: User, contact: User) = {
    this.create.user(user).contact(contact).time(new Date()).save
  }
}

/**
 * An O-R mapped "Contact" class
 */
class Contact extends LongKeyedMapper[Contact] {
  def getSingleton = Contact // what's the "meta" server

  // poke id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // poke from user
  object user extends LongMappedMapper(this, User)

  // poke to user
  object contact extends LongMappedMapper(this, User)

  // when the contact relationship was created
  object time extends MappedDateTime(this)
}
