package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 

object FileType extends FileType with LongKeyedMetaMapper[FileType] {
   override def dbTableName = "FileTypes" // define the DB table name
}

/**
 * An O-R mapped "Post" class
 */
class FileType extends LongKeyedMapper[FileType] {
  def getSingleton = FileType // what's the "meta" server

  // poke id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // type
  object fileType extends MappedString(this, 60)
}
