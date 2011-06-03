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

class PokeSender {
  def userId = UserHelpers.userId
  def currUser = User.currentUser
  def user = UserHelpers.getUser(UserHelpers.userId)

  def render = {
    def process(cu: User, u: User): JsCmd = {
      PokeMaster.createPoke(cu, u)
      Noop
    }

    
    (currUser, user) match {
      case (Full(cu), Full(u)) => {
	if (cu == u) {
	  "*" #> ClearNodes // dont render if on your own profile page
	} else if (u.isPokedBy(cu)) {
	  "*" #> ClearNodes // dont render if already poked
	} else {
	  "type=submit" #> ajaxButton(
	    "Poke", () => {process(cu, u); }
	  ) 
	}
      }
      case _ => "*" #> ClearNodes // don't render if error
    }    
  }
}
