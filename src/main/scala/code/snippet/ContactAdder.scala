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


// TODO: rerender page on contact add
class ContactAdder {
  def userId = UserHelpers.userId
  def currUser = User.currentUser
  def user = UserHelpers.getUser(UserHelpers.userId)

  def render = {
    def process(cu: User, u: User): JsCmd = {
      val stat = cu.addContact(u);
      ActivityMaster ! RefreshHome(cu)
	  
      if (stat == 0) {
	S.notice("You have added " + u.firstName.is + " to your contacts");
      } else {
	S.notice("User is already on your contact's list");
      }

      Noop
    }

    
    (currUser, user) match {
      case (Full(cu), Full(u)) => {
	if (cu == u) {
	  "*" #> ClearNodes // dont render if on your own profile page
	} else if (cu.isUsersContact(u)) {
	  "*" #> ClearNodes // dont render if already a contact
	} else {
	  "type=submit" #> ajaxButton(
	    "Add as contact", () => {process(cu, u); }
	  ) 
	}
      }
      case _ => "*" #> ClearNodes // don't render if error
    }    
  }
}
