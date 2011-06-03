package code.comet

import net.liftweb._
import net.liftweb.common.{Box,Full,Empty}
import http._
import scala.xml._
import net.liftweb.util._
import Helpers._
import actor._

import code.comet._
import code.model._
import code.snippet._

class ContactsViewer extends CometActor with CometListener {
  def registerWith = ContactsMaster

  def currentUser = UserHelpers.getUser(UserHelpers.currUserId)
  
  def renderUser(user : User) = {
    def processRemove() = {
      for (cu <- currentUser) {
	ContactsMaster.removeContact(cu, user)
	ActivityMaster ! RefreshHome(cu)
      }
    }
	  
    ".prpicThumb [src]" #> RendererHelper.getProfilePictureUrl(user) &
    ".name *" #> user.shortName & 
    ".aboutMe *" #> user.aboutMe.is & 
    ".name [href]" #> "/profile?id=".concat(user.id.is.toString) &
    "type=submit" #> SHtml.onSubmitUnit(processRemove) &
    "type=submit [value]" #> "Remove"
  }

  def render = {
    currentUser match {
      case Full(u) =>
	val contacts = u.getContacts()
      
	".contactResult *" #> contacts.map(
	  user => renderUser(user))
      case _ => "*" #> ""
    }
  }

  override def lowPriority = {
    case s: ContactSig => {
      reRender(true)
    }
  }
}
