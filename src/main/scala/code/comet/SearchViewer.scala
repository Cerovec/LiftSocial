package code.comet

import net.liftweb._
import http._
import util._
import Helpers._
import actor._
import common.{Box, Full, Empty, Failure}
import java.util.Date
import mapper._

import code.model._
import code.snippet._

class SearchViewer extends CometActor with CometListener {
  def registerWith = SearchMaster

  def currentUser = UserHelpers.getUser(UserHelpers.currUserId)

  val default = "####";
  var query = default;

  override def lowPriority = {
    case ss: SearchSig => {
      query = ss.s
      reRender(true)
    }
  }
  
  def renderone(user : User) = {
    def processRemove() = {
      for (cu <- currentUser) {
	ContactsMaster.removeContact(cu, user)
	ActivityMaster ! RefreshHome(cu)
	SearchMaster.makeSearch(query)
      }
    }

    def processAdd() = {
      for (cu <- currentUser) {
	ContactsMaster.addContact(cu, user)
	ActivityMaster ! RefreshHome(cu)
	SearchMaster.makeSearch(query)
      }
    }

    def submitBtn = {
      currentUser match {
	case Full(cu) => SHtml.onSubmitUnit(processRemove)
	case _ => ""
      }
    }

    def submitBtnValue = {
      currentUser match {
	case Full(cu) => "Remove"
	case _ => ""
      }
    }

    def isFullUser = {
      currentUser match {
	case Full(cu) => true;
	case _ => false;
      }
    }
    
    ".prpicThumb [src]" #> RendererHelper.getProfilePictureUrl(user) &
    (if (!isFullUser || currentUser.open_! == user) {
      ".name *" #> user.shortName & 
      ".aboutMe *" #> user.aboutMe.is & 
      ".name [href]" #> "/profile?id=".concat(user.id.is.toString) &
      ClearClearable
    } else {
      val cu = currentUser.open_!

      ".name *" #> user.shortName & 
      ".aboutMe *" #> user.aboutMe.is & 
      ".name [href]" #> "/profile?id=".concat(user.id.is.toString) &
      "type=submit" #> (if (cu.isUsersContact(user)) 
			  SHtml.onSubmitUnit(processRemove)
			else SHtml.onSubmitUnit(processAdd)) & 
      "type=submit [value]" #> (if (cu.isUsersContact(user)) "Remove"
				else "Add as contact")
    })
      
//    "type=submit" #> (if (!isFullUser) Empty:Box[String] 
//		      else SHtml.onSubmitUnit(processAdd)) &
//    "type=submit [value]" #> (if (!isFullUser) "" else "Add as contact")

  }

  def render = {
    ".searchResult *" #> User.findUsersLike(query).map(
      user => renderone(user)
    )
  }
}
