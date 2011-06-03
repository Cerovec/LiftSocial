package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 

object File extends File with LongKeyedMetaMapper[File] {
   override def dbTableName = "Files" // define the DB table name
}

/**
 * An O-R mapped "File" class
 */
class File extends LongKeyedMapper[File] {
  def getSingleton = File // what's the "meta" server

  // file id
  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  // lookup id
  object lookup extends MappedUniqueId(this, 32) { 
    override def dbIndexed_? = true 
  } 

  // user that published the file
  object fromUser extends LongMappedMapper(this, User)

  // activity that the file was published under
  object activity extends LongMappedMapper(this, Activity)

  // when the file  was created
  object saveTime extends MappedLong(this) { 
    override def defaultValue = millis 
  }   

  // data of the file
  object data extends MappedBinary(this)
 
  // mimetype
  object mimeType extends MappedString(this, 256)
}

object FileServicer {
  object TestFile { 
    def unapply(in: String): Option[File] = {
      println("\n\n\n" + in + "\n\n\n")
      File.find(By(File.lookup, in.trim))
    } 
  }

  private def findFromRequest(req: Req): Box[File] = {
    val toFind = req.path.wholePath.last.toLong
    println("\n\n\n" + toFind + "\n\n\n")
    val file = File.find(By(File.id, toFind))
    file match {
      case Full(f) => println("\n" + f.id + "\n" + f.mimeType + "\n\n")
      case _ => println("\nNo file found\n\n")
    }
    file
  }

  //def matcher2: LiftRules.DispatchPF = { 
   // case r @ Req("file" :: TestFile(img) :: Nil, _, GetRequest) 
//	      => () => serveFile(img, r) 
  //}

  def matcher: LiftRules.DispatchPF = {
    case req @ Req("file" :: _ :: Nil, _, GetRequest) if findFromRequest(req).isDefined => () => serveFile(req)
  }

  val MimeExtractorRE = """/""".r

  def serveFile(r: Req): Box[LiftResponse] = { 
    val file = findFromRequest(r).open_! // we just tested the guard

   /* if (r.testIfModifiedSince(file.saveTime.is+1)) {
        println("\nunmodified\n")
        Full(InMemoryResponse(
	  new Array[Byte](0), List("Last-Modified" -> 
				   toInternetDate(file.saveTime.is)), Nil, 304))
    } else {*/
      println("\nmodified " + file.data.length + " \n")
      val mime: List[String] = file.mimeType.is.split("/").toList
      val headers = mime match {
	case "application" :: ext :: Nil => {
	  val filename = file.id.is.toString + "." + ext 
	  println("\n\n Ime datoteke: " + filename + "\n")
	  List("Last-Modified" -> toInternetDate(file.saveTime.is), 
               "Content-Type" -> file.mimeType.is, 
               "Content-Length" -> file.data.is.length.toString,
	       "Content-Disposition" -> ("attachment; filename=" + filename))
	}
	case _ => List("Last-Modified" -> toInternetDate(file.saveTime.is), 
                       "Content-Type" -> file.mimeType.is, 
                       "Content-Length" -> file.data.is.length.toString)
      }
      Full(InMemoryResponse(file.data.is, headers, Nil, 200))
   //}
  }

  def saveFile(fp: FileParamHolder): Box[File] = { 
    fp.file match { 
      case null => Empty
      case x if x.length == 0 => Empty
      case x => 
	println("\n\n\nImam novi faaaaajl!")
	println(fp.mimeType + "\n")
	println(x.length + "\n")
        val file = File.create.data(x)
	file.mimeType(fp.mimeType)
      // fromUser
      // activity
      // savetime
	file.save
	Full(file)
    }
  }

  def saveProfilePic(user: User, fp: FileParamHolder): Unit = { 
    fp.file match { 
      case null => 
      case x if x.length == 0 => 
      case x => 	
        val file = File.create.data(x)
	file.mimeType(fp.mimeType)
        file.fromUser(user)
	// savetime set to millis
	// activity doesn't exist
	file.save
      	user.profilePicture(file)
	user.save
    } 
  } 
}
