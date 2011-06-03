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

object RendererHelper {
  def getProfilePictureUrl(user: User) = {
    if (user.profilePicture == user.profilePicture.defaultValue) {
      if (user.sex == Genders.Female) {
	"/file/femprof.jpg"
      } else {
	"/file/maleprof.gif"
      }
    } else {
      ("file/" + user.profilePicture.toString)
    }
  }

  def getFileUrl(file: File) = {
    "file/" + file.id.toString
  }
}
