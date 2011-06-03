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

class ContactSuggestionsViewer extends CometActor with CometListener  {
  def registerWith = ContactsMaster

  def currentUser = UserHelpers.getUser(UserHelpers.currUserId)

  val default = 5;
  val increase = 3;
  var num = default;
  
  def renderUser(user : User) = {
    def processAdd() = {
      for (cu <- currentUser) {
	ContactsMaster.addContact(cu, user)
	ActivityMaster ! RefreshHome(cu)
      }
    }

    ".prpicThumb [src]" #> RendererHelper.getProfilePictureUrl(user) &
    ".name *" #> user.shortName & 
    ".aboutMe *" #> user.aboutMe.is & 
    ".name [href]" #> "/profile?id=".concat(user.id.is.toString) &
    "type=submit" #> SHtml.onSubmitUnit(processAdd) &
    "type=submit [value]" #> "Add as contact"  
  }

  def render = {
    currentUser match {
      case Full(u) =>
	val contacts = u.getRecommendedContacts(num)

	".contactResult *" #> contacts.map(
	  user => renderUser(user))
      case _ => "*" #> ""
    }
  }

  override def lowPriority = {
    case s: ContactSig => {
      reRender(true)
    }
    case s: MoreSuggestionsSig => {
      num = num + increase
      reRender(true)
    }
    case s: ResetSuggestionsNumSig => {
      num = default
      reRender(true)
    }
  }
}
