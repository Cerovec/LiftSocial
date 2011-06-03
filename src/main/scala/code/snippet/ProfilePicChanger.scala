package code.snippet

import net.liftweb._
import net.liftweb.common.{Box,Full,Empty,Failure}
import code.model._
import code.comet._
import scala.xml._
import net.liftweb.util._
import Helpers._
import _root_.net.liftweb.http._
import _root_.net.liftweb.mapper._
import js._
import JsCmds._
import JE._
import _root_.net.liftweb.http.SHtml._
import java.util.Date

class ProfilePicChanger {
  def currUser = User.currentUser

  val max = 1024*1024

  def render = {
    def process(cu: User, fp: FileParamHolder) = {
      if (fp.file == null || fp.file.length == 0 || fp.file.length > max) {
	S.error("No empty or files larger than " + 
		(max/1024/1024).toInt.toString + " MB")
      } else {
	FileServicer.saveProfilePic(cu, fp)
	S.notice("Thanks for the upload") 
        S.redirectTo("/profile") 
      }
    }

    currUser match {
      case Full(cu) => {
	"#profpic [src]" #> RendererHelper.getProfilePictureUrl(cu) &
	"type=file" #> SHtml.fileUpload(fp => process(cu, fp))
      }				      
      case _ => "*" #> ClearNodes // don't render if error
    }    
  }
}
