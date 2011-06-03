package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 

import java.util.Date

object Activity extends Activity with LongKeyedMetaMapper[Activity] {
  override def dbTableName = "Activities" // define the DB table name

  // function creates a post
  // if a ActivityType with name post doesn't exist, it returns Empty
  def createPost(text: String): Box[Activity] = {
    var post = Activity.create
    post.text(text)
    post.time(new Date())

    val postType = ActivityType.find(By(ActivityType.typeString, "post"))
    postType match {
      case Full(t) => {
	post.activityType(t)
	Full(post)
      }
      case _ => Empty
    }
  }

  // function creates a blog post
  // if a ActivityType with name blog doesn't exist, it returns Empty
  def createBlogPost(subject: String, text: String, filebox: Box[FileParamHolder], 
		     keywordsList:List[String]): Box[Activity] = {
    var post = Activity.create
    post.subject(subject)
    post.text(text)
    post.time(new Date())
    println("Saveam datoteku\n" + filebox + "\n")
    //post.keywords = 
    for (fp <- filebox) {
      var savedfilebox = FileServicer.saveFile(fp)
      for (file <- savedfilebox) {
	post.file(file)
      }

      println("rezultat saveanja je " + savedfilebox + "\n");
    }

    val postType = ActivityType.find(By(ActivityType.typeString, "blog"))
    postType match {
      case Full(t) => {
	post.activityType(t)
	Full(post)
      }
      case _ => {
	Empty
      }
    }
  }
}

/**
 * An O-R mapped "Activity" class
 */
class Activity extends LongKeyedMapper[Activity] 
    with OneToMany[Long, Activity] with ManyToMany {
  
  def getSingleton = Activity // what's the "meta" server

  // activity id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // user that published the activity
  object user extends LongMappedMapper(this, User)

  // when the activity was created
  object time extends MappedDateTime(this)

  // subject
  object subject extends MappedText(this)

  // text
  object text extends MappedText(this)

  // activity type
  object activityType extends LongMappedMapper(this, ActivityType)

  // attached files
  object file extends LongMappedMapper(this, File)

  // keywords
  object keywords extends MappedManyToMany(
    ActivityKeyword, ActivityKeyword.activity, ActivityKeyword.keyword, Keyword)
}
